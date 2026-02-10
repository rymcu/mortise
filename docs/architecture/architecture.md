# 架构说明

本项目采用多模块单体 + 业务域 DDD 分层架构，强调模块边界清晰、依赖单向、接口统一、SPI 可扩展。

> **技术栈**: Spring Boot 3.5.7 · Java 21 · PostgreSQL 17 · Redis · MyBatis-Flex 1.11.0  
> **当前版本**: 0.2.0 · **模块总数**: 25（含子模块）

## 分层概览

| 层级 | 模块 | 定位 |
|------|------|------|
| L6 应用层 | mortise-app | Spring Boot 启动入口、Flyway 迁移、多环境配置 |
| L5 业务域 API | *-admin / *-api | REST 接口定义、DTO 校验、安全配置 |
| L4 业务域应用与基础设施 | *-application / *-infra / *-domain | DDD 领域模型、业务服务、持久化实现 |
| L3 应用基础层 | mortise-auth / mortise-web-support / mortise-monitor / mortise-file / mortise-wechat | 认证授权、Web 基础、监控、文件、微信集成 |
| L2 基础设施层 | mortise-log / mortise-cache / mortise-notification / mortise-persistence | 日志、缓存、通知、数据库扩展 |
| L1 核心层 | mortise-common / mortise-core | 通用工具、异常体系、统一返回、虚拟线程配置 |

## 模块职责

| 模块 | 职责 | 关键实现 |
| --- | --- | --- |
| **mortise-common** | 通用工具与异常 | ULID 生成器、BeanCopier、异常体系（ServiceException/BusinessException）、枚举常量（Status/DelFlag/MenuType） |
| **mortise-core** | 核心抽象 | `GlobalResult<T>` 统一返回、`ResultCode` 结果码、`CurrentUser` SPI、JDK 21 虚拟线程 TaskExecutor、Jasypt 加密配置 |
| **mortise-log** | 日志与审计 | `@OperationLog` / `@ApiLog` AOP 注解、`LogStorage` SPI（数据库/文件双实现）、`ClientTypeResolver` 责任链 |
| **mortise-cache** | 缓存能力 | `CacheService` 统一接口（String/Hash/Set/TTL）、Redis 实现、`CacheConfigurer` SPI（模块 TTL 注册）、`CacheExpirationHandler` SPI（键过期事件分发） |
| **mortise-notification** | 通知能力 | `NotificationSender` SPI、邮件发送（Spring Mail + Thymeleaf）、微信模板消息发送、异步/批量发送 |
| **mortise-persistence** | 数据持久化 | MyBatis-Flex 配置、PostgreSQL JSONB / Duration / StringList 自定义类型处理器、`DatabaseLogStorage`（LogStorage SPI 实现） |
| **mortise-auth** | 认证授权 | JWT 无状态认证（JJWT 0.12.5）、Spring Security 6 + `SecurityConfigurer` SPI（6 个实现模块注册安全规则）、OAuth2 多平台（GitHub/Google/Logto/微信 Strategy 模式）、短信验证码登录、扫码登录（Spring Event）、多用户表认证（`CustomUserDetailsService` SPI） |
| **mortise-web-support** | Web 支撑 | `@AdminController` / `@ApiController` 元注解、`GlobalExceptionHandler` 全局异常处理、`@RateLimit` Resilience4j 限流（IP/用户/方法/SpEL）、`JacksonConfigurer` SPI、SpringDoc OpenAPI 自动文档 |
| **mortise-monitor** | 监控与指标 | Micrometer 公共标签、Prometheus 指标导出、自定义 DB/Redis HealthIndicator、启动性能监控、Actuator 安全配置 |
| **mortise-file** | 文件管理 | x-file-storage 集成、阿里云 OSS 适配、分片上传、文件元数据 DB 持久化、文件删除事件监听 |
| **mortise-wechat** | 微信集成 | 多账号公众号管理（动态服务实例）、微信开放平台接入、消息路由（关注/取关/扫码/菜单/文本等）、扫码登录、模板消息推送、`WeChatNotificationSender`（NotificationSender SPI 实现） |
| **mortise-test-support** | 测试支撑 | 共享测试环境配置 |
| **mortise-system-domain** | 系统领域 | User/Role/Menu/Dict/DictType/UserRole/RoleMenu/UserOAuth2Binding 实体、`@DictFormat` 字段级字典翻译注解 |
| **mortise-system-infra** | 系统基础设施 | MyBatis-Flex Mapper 接口、`SystemCacheConfigurer`（CacheConfigurer SPI）、`SystemLogStorage` |
| **mortise-system-application** | 系统应用 | UserService / AuthService / RoleService / MenuService / DictService / PermissionService / SystemInitService、Spring Event 事件驱动（登录/注册/重置密码/OIDC 用户）、`UserOnlineStatusExpirationHandler`（CacheExpirationHandler SPI） |
| **mortise-system-admin** | 系统管理端 | AuthController / UserController / RoleController / MenuController / DictController / Oauth2ClientConfigController 等、`DictSerializer` 字典翻译序列化、`SystemSecurityConfigurer` / `DictJacksonConfigurer` SPI 实现 |
| **mortise-system-api** | 系统公开 API | 预留扩展（当前无接口） |
| **mortise-member-domain** | 会员领域 | Member / MemberOAuth2Binding 实体、JSR-380 校验注解 |
| **mortise-member-infra** | 会员基础设施 | MemberMapper / MemberOAuth2BindingMapper |
| **mortise-member-application** | 会员应用 | MemberService / MemberOAuth2BindingService |
| **mortise-member-admin** | 会员管理端 | MemberController 管理端接口、AdminMemberService |
| **mortise-member-api** | 会员公开 API | MemberAuthController（注册/登录/手机登录）、OAuth2AuthController、FileController、`MemberDetailsServiceImpl`（CustomUserDetailsService SPI）、`AppSecurityConfigurer`（SecurityConfigurer SPI） |
| **mortise-app** | 应用装配 | `@SpringBootApplication` 启动类、Flyway SQL 迁移脚本、多环境配置（dev/local/prod）、MapperScan 全局包扫描 |

## SPI 接口矩阵

| SPI 接口 | 定义模块 | 用途 | 已有实现 |
|---|---|---|---|
| `LogStorage` | mortise-log | 日志存储策略 | DatabaseLogStorage (persistence)、SystemLogStorage (system-infra) |
| `ClientTypeResolver` | mortise-log | 客户端类型识别 | DefaultClientTypeResolver |
| `CacheConfigurer` | mortise-cache | 模块缓存 TTL 注册 | SystemCacheConfigurer (system-infra)、AuthCacheConfigurer (auth) |
| `CacheExpirationHandler` | mortise-cache | 缓存过期事件处理 | UserOnlineStatusExpirationHandler (system-application) |
| `SecurityConfigurer` | mortise-auth | 模块安全规则注册 | SystemSecurityConfigurer / AppSecurityConfigurer / WebSecurityConfigurer / MonitorSecurityConfigurer / FileSecurityConfigurer / WeChatSecurityConfigurer |
| `CustomUserDetailsService` | mortise-auth | 多用户表认证 | UserDetailsServiceImpl (system)、MemberDetailsServiceImpl (member) |
| `UserTypeResolver` | mortise-auth | 用户类型识别 | DefaultUserTypeResolver |
| `OAuth2ProviderStrategy` | mortise-auth | OAuth2 平台适配 | GitHubProviderStrategy / GoogleProviderStrategy / WeChatProviderStrategy / LogtoProviderStrategy |
| `OAuth2LoginSuccessHandlerProvider` | mortise-auth | OAuth2 登录成功处理 | SystemOAuth2LoginSuccessHandlerProvider / ApiOAuth2LoginSuccessHandlerProvider |
| `NotificationSender` | mortise-notification | 通知渠道扩展 | EmailNotificationSender / WeChatNotificationSender |
| `JacksonConfigurer` | mortise-web-support | Jackson 序列化扩展 | DictJacksonConfigurer (system-admin) |
| `CurrentUser` | mortise-core | 当前用户抽象 | UserDetailInfo (system)、MemberDetailInfo (member) |

## 依赖矩阵

| 层级 | 允许依赖 | 禁止依赖 |
| --- | --- | --- |
| L6 应用层 | L1-L5 全部模块 | — |
| L5 业务域 API | L3-L4 与本域 domain/application/infra | 跨域模块 |
| L4 业务域应用与基础设施 | L1-L3 与本域 domain | L5、同层跨域 |
| L3 应用基础层 | L1-L2 | L4-L6 |
| L2 基础设施层 | L1 | L3-L6 |
| L1 核心层 | 无 | 所有上层模块 |

## Web 规范

- 使用 `@AdminController` 标记管理端接口，自动添加 `/api/v1/admin` 前缀。
- 使用 `@ApiController` 标记公开 API，自动添加 `/api/v1` 前缀。
- OpenAPI 分组自动生成：`admin`（匹配 `/api/v1/admin/**`）与 `api`（匹配 `/api/v1/**`）。
- `@RateLimit` 注解支持 IP / 用户 / 方法 / SpEL 多维限流策略。
- `GlobalExceptionHandler` 统一处理 `BusinessException`、`ServiceException`、`AuthenticationException`、校验异常等。

## 设计模式应用

| 模式 | 应用场景 |
|------|----------|
| **SPI 扩展** | 缓存/日志/安全/通知/OAuth2/Jackson 等 12 组接口，模块化热插拔 |
| **Strategy 策略** | OAuth2ProviderStrategy（GitHub/Google/WeChat/Logto 平台适配） |
| **Chain of Responsibility 责任链** | ClientTypeResolver 客户端识别、UserTypeResolver 用户类型识别 |
| **Observer 事件驱动** | Spring Event（登录/注册/重置密码/OIDC 用户/扫码确认） |
| **Template Method 模板方法** | AbstractHandler（微信消息处理基类）、AbstractBuilder（微信回复构建） |

## 开发约束

- 同层不互相依赖。
- 业务层不依赖 Web 相关类型。
- DTO 校验放在 API 层，领域模型保持纯净。
- 构造器注入为主，依赖字段声明为 `private final`。
- 异步任务默认使用 JDK 21 虚拟线程执行器。
