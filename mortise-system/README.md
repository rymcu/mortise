# mortise-system

## 模块介绍

`mortise-system` 是 Mortise 的系统管理基础域模块，承载用户、角色、菜单、字典、认证、OAuth2、站点配置、日志查询等核心系统功能。作为最大、最复杂的业务域模块，它为其他所有业务模块提供用户与权限的基础能力。

该模块采用 Mortise 标准五层拆分方式：`domain / application / infra / admin / api`，并在 Application 层全面推进 CQRS（Command/Query 职责分离）重构，同时通过 Facade + Assembler 模式隔离 Controller 与业务服务。

## 架构设计

### 模块分层

```text
mortise-system/
├── mortise-system-domain        领域对象、仓储端口、查询模型
├── mortise-system-application   CQRS 服务（Command/Query）、事件、遗留混合服务
├── mortise-system-infra         Mapper、持久化实体、仓储实现
├── mortise-system-admin         管理端控制器、Facade、Assembler、DTO
└── mortise-system-api           用户端公开 API（当前较少接口）
```

### 层内职责

- `mortise-system-domain`：定义纯领域实体（`entity.*`：`User`、`Role`、`Menu`、`Dict`、`DictType`、`RoleMenu`、`UserRole`、`UserOAuth2Binding`），仓储端口接口（`repository.*`：8 个），查询条件模型（`model.*`：`UserSearch`、`RoleSearch`、`MenuSearch`、`DictSearch`、`DictTypeSearch`、`LogSearch` 等），自定义注解（`annotation.*`），领域异常（`exception.*`）；不依赖 MyBatis-Flex、Spring Web。
- `mortise-system-application`：承载 CQRS 风格的应用服务——Command 服务（`service.command.*`：`UserCommandService`、`RoleCommandService`、`MenuCommandService`、`DictCommandService`、`DictTypeCommandService`）处理写操作，Query 服务（`query.*`：`UserQueryService`、`RoleQueryService`、`MenuQueryService`、`DictQueryService`、`DictTypeQueryService`、`LogQueryService`）处理读操作；另含认证（`AuthService`）、权限（`PermissionService`）、缓存（`SystemCacheService`）、初始化（`SystemInitService`）、站点配置（`SiteConfigService`）等专项服务；以及领域事件（`handler.event.*`）和异步事件处理器（`handler.*`）。
- `mortise-system-infra`：承载 MyBatis-Flex Mapper、持久化实体（PO）、仓储实现；负责 Domain Entity 与 PO 之间的转换。
- `mortise-system-admin`：提供管理端接口；Controller 通过 Facade（`facade.*`：`UserAdminFacade`、`RoleAdminFacade`、`MenuAdminFacade`、`DictAdminFacade`、`DashboardAdminFacade`、`AuthAdminFacade` 等）调用 Application，Assembler（`assembler.*`）负责 DTO 与领域对象转换。Controller 不直接依赖 Service/Entity/Mapper。
- `mortise-system-api`：提供用户端公开 API，当前接口较少。

### CQRS 分离

Application 层正在从遗留混合 Service 逐步迁移至 Command/Query 分离模式：

- **Command 路径**：`service.command.*`（User/Role/Menu/Dict/DictType 的增删改操作）
- **Query 路径**：`query.*`（User/Role/Menu/Dict/DictType/Log 的查询操作）
- **遗留服务**：`service.*` 下仍存在部分混合 Service（如 `UserService`、`RoleService` 等），正通过 ArchUnit 测试逐步约束 Facade 使用 Command/Query 而非遗留 Service。

### 事件驱动

Application 层通过 Spring 事件实现跨关注点的异步处理：

- `UserLoginEvent` → `UserLoginEventHandler`（`@Async @EventListener`，登录追踪）
- `RegisterEvent` → `RegisterHandler`（注册后处理）
- `ResetPasswordEvent` → `ResetPasswordHandler`（密码重置通知）
- `OidcUserEvent` → `OidcUserEventHandler`（OIDC 用户同步）
- `UserOnlineStatusExpirationHandler`（在线状态过期管理）

### 架构守卫

- Maven Enforcer：Domain 层开启 `layer.guard.domain.skip=false`，仅依赖 `mortise-common` 和 `mortise-core`；Application 层开启 `layer.guard.application.skip=false`，禁止依赖 Infra；Admin 层开启 `layer.guard.admin-api.skip=false`，禁止直接依赖 Domain/Infra 模块。
- ArchUnit：共 **11 个**架构测试（位于 `mortise-system-admin` 测试目录）：
  - 分层契约：`system_module_respects_current_layering_contract()`
  - Application → Mapper 隔离：重构后的服务禁止直接访问 Mapper
  - Controller → Facade 约束：15 个 Controller 必须通过 Facade 调用，不允许直接触达 Service/Query/Entity/Assembler
  - Facade → Controller 反向隔离：11 个 Facade 禁止依赖 Controller
  - CQRS 强制执行：Dict/Menu/Role/User/Dashboard/Auth 各 Facade 必须使用 Command/Query 契约而非混合 Service
  - 内部组件隔离：Serializer 等支撑组件使用 Query 契约而非混合 Service
  - 应用层内部组件迁移检查：逐步约束所有内部组件使用 Command/Query 契约

### 与其他模块的关系

- `mortise-auth`：Application 层依赖 `mortise-auth` 的认证链（OAuth2、JWT）
- `mortise-cache`：Application 层使用 `mortise-cache` 的缓存策略（用户信息、字典缓存）
- `mortise-log`：Application 层依赖 `mortise-log` 的日志存储与查询
- `mortise-notification`：Application 层使用 `mortise-notification` 发送密码重置等通知
- `mortise-file`：Admin 层依赖 `mortise-file` 的文件上传/管理能力
- `mortise-web-support`：Admin 和 API 控制器依赖 `@AdminController`、`@ApiController` 元注解和 `GlobalResult` 返回包装
- `mortise-core`：Domain 和 Application 依赖通用分页模型和基础工具

## 已落地接口

### 管理端

**用户管理** (`/api/v1/admin/users`)
- `GET /`：用户分页查询 — `system:user:list`
- `GET /{id}`：用户详情 — `system:user:query`
- `POST /`：新增用户 — `system:user:add`
- `PUT /{id}`：更新用户 — `system:user:edit`
- `PATCH /{id}/status`：更新用户状态 — `system:user:edit`
- `POST /{id}/reset-password`：重置密码 — `system:user:reset-password`
- `POST /{id}/roles`：分配角色 — `system:user:assign-role`
- `GET /{id}/roles`：查询用户角色 — `system:user:query`
- `DELETE /{id}`：删除用户 — `system:user:delete`
- `DELETE /batch`：批量删除 — `system:user:delete`

**角色管理** (`/api/v1/admin/roles`)
- 标准 CRUD + 角色菜单分配接口 — `system:role:*`

**菜单管理** (`/api/v1/admin/menus`)
- 标准 CRUD + 菜单树接口 — `system:menu:*`

**字典管理** (`/api/v1/admin/dictionaries`, `/api/v1/admin/dictionary-types`)
- 标准 CRUD — `system:dict:*`, `system:dict-type:*`

**认证** (`/api/v1/admin/auth`)
- `GET /oauth2-providers`：获取 OAuth2 提供商列表（公开）
- `POST /login`：登录（`@RateLimit`）
- `POST /register`：注册（`@RateLimit`）

**短信认证** (`/api/v1/admin/auth/sms`)
- `POST /send`：发送短信验证码（`@RateLimit` 3次/5分钟）
- `POST /login`：短信验证码登录（`@RateLimit` 5次/5分钟）

**OAuth2 配置** (`/api/v1/admin/oauth2/client-configs`)
- 标准 CRUD + 批量更新 — `system:oauth2-client:*`

**OAuth2 扫码** (`/api/v1/admin/oauth2/qrcode`)
- `GET /wechat/{registrationId}`：获取微信扫码 URL
- `GET /state/{state}`：查询扫码状态

**系统管理**
- `/api/v1/admin/dashboard/stats`：仪表盘统计
- `/api/v1/admin/system-init`：系统初始化（状态查询、执行、进度）
- `/api/v1/admin/system/log`：操作日志与 API 日志查询
- `/api/v1/admin/system/site-config`：站点配置管理（分组查询/保存）
- `/api/v1/admin/system/cache`：缓存管理（按用户/字典类型清理）
- `/api/v1/admin/files`：文件管理（列表、上传、删除）
- `/api/v1/admin/notification/channels`：通知渠道配置管理
