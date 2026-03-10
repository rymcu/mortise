---
title: API 参考
description: 站点端当前实际使用的认证、OAuth2、文件与公开配置接口
order: 3
---

# API 参考

Mortise 后端统一挂载在 `/mortise/api/v1`。对 `apps/site` 而言，最关键的是会员认证、OAuth2、文件上传和站点公开配置这几组接口。

## 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 成功，非 200 表示错误 |
| message | string | 提示信息 |
| data | any | 响应数据 |

## 认证

受保护接口需要在请求头携带 Bearer Token：

```http
Authorization: Bearer <access_token>
```

`apps/site` 当前运行时默认使用以下接口：

| 配置项 | 路径 |
|--------|------|
| 登录 | `/api/v1/app/auth/login` |
| 刷新 Token | `/api/v1/app/auth/refresh-token` |
| OAuth2 回调兑换 Token | `/api/v1/app/oauth2/callback` |
| 当前会员信息 | `/api/v1/app/auth/profile` |
| OAuth2 授权地址 | `/api/v1/app/oauth2/auth-url/{registrationId}` 或相关微信接口 |

Token 过期后，前端会优先走刷新链路而不是直接把页面跳回登录页。

## 会员认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/app/auth/register` | 会员注册 |
| POST | `/api/v1/app/auth/login` | 账号密码登录 |
| POST | `/api/v1/app/auth/login-by-phone` | 手机验证码登录 |
| POST | `/api/v1/app/auth/send-code` | 发送验证码 |
| POST | `/api/v1/app/auth/verify-code` | 校验验证码 |
| POST | `/api/v1/app/auth/refresh-token` | 使用 refresh token 刷新访问令牌 |
| POST | `/api/v1/app/auth/refresh-token-by-jwt` | 使用当前 JWT 刷新令牌 |
| GET | `/api/v1/app/auth/profile` | 获取当前会员信息 |
| PUT | `/api/v1/app/auth/profile` | 更新当前会员信息 |

## OAuth2 与微信登录接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/oauth2/wechat/qrcode` | 获取 PC 端微信扫码授权链接 |
| GET | `/api/v1/app/oauth2/qrcode/state/{state}` | 查询扫码或授权状态 |
| GET | `/api/v1/app/oauth2/wechat/mobile/auth-url` | 获取手机端微信授权 URL |
| GET | `/api/v1/app/oauth2/auth-url/{registrationId}` | 获取指定 OAuth2 客户端授权地址 |
| GET | `/api/v1/app/oauth2/callback?state=...` | 使用 state 兑换登录结果 |
| POST | `/api/v1/app/oauth2/mp/qrcode` | 创建公众号扫码登录二维码 |
| GET | `/api/v1/app/oauth2/wechat/silent/auth-url` | 获取静默授权地址 |

这些接口主要服务于 `apps/site` 的微信登录、GitHub 登录和扫码登录流程。不同客户端的选择逻辑由前端页面和 `@mortise/auth` 运行时统一处理。

## 站点公开配置接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/system/site-config/public` | 获取站点公开配置，如站点名称、Logo、Favicon 等 |

虽然接口位于 `admin` 前缀下，但该接口已被显式放行，供站点前端启动时无鉴权读取公开信息。

## 文件接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/app/files` | 会员文件上传，表单字段名为 `file` |

当前仓库中的会员文件控制器只暴露上传接口。若页面拿到的是相对媒体路径，展示层应继续通过既有媒体 URL 工具转换，不要在页面里手工拼接公共地址。

## 访问方式示例

### 登录

```http
POST /mortise/api/v1/app/auth/login
Content-Type: application/json

{
  "account": "demo",
  "password": "123456"
}
```

### 获取当前会员信息

```http
GET /mortise/api/v1/app/auth/profile
Authorization: Bearer <access_token>
```

### 上传文件

```bash
curl -X POST http://localhost:9999/mortise/api/v1/app/files \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@avatar.png"
```

## 在线文档

完整接口文档以 Swagger UI 为准：

`http://localhost:9999/mortise/swagger-ui/index.html`

如果站点文档和运行中的接口不一致，优先以当前后端控制器和 Swagger 输出为准。
*** Add File: e:\workspace\mortise\frontend\apps\site\content\docs\frontend-quick-start.md
---
title: 前端快速上手
description: apps/admin、apps/site、packages 与 Layer 的最短开发路径
order: 4
---

# 前端快速上手

本页面向第一次进入 `frontend/` 工作区的开发者，重点覆盖目录结构、启动命令和几条容易踩坑的约束。

## 前置条件

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | 20+ | 推荐 22.x LTS |
| pnpm | 10+ | 前端工作区只支持 pnpm |
| Git | 2.30+ | Layer / submodule 场景需要 |

后端默认监听：`http://localhost:9999/mortise`

## 安装依赖

```bash
cd frontend
pnpm install
```

pnpm workspace 会一次安装 apps 和 packages，无需逐个进入子目录。

## 启动开发服务器

```bash
# 管理端
pnpm dev:admin

# 站点端
pnpm dev:site
```

访问地址：

| 应用 | 地址 |
|------|------|
| 管理端 | `http://localhost:3000/admin/` |
| 站点端 | `http://localhost:3001/` |

## 工作区结构

```text
frontend/
├── apps/
│   ├── admin/
│   └── site/
├── packages/
│   ├── auth/
│   ├── core-sdk/
│   ├── ui/
│   └── config/
└── layers/
    ├── base/
    ├── community/
    └── commerce/
```

边界规则：

- 跨 app 复用逻辑放 `packages/*`
- 可选业务能力优先放 `layers/*`
- 页面层不要重复造鉴权和 API 封装

## 开发约束

### API 调用

- 页面层优先复用 `@mortise/core-sdk`
- Token 注入、刷新、会话恢复交给 `@mortise/auth`
- 不要在页面组件里到处拼接完整后端 URL

### 路由与登录

- 站点端登录页路由是 `/auth/login`
- 管理端登录页路由是 `/admin/auth/login`
- 需要鉴权的站点页面使用 `middleware/auth`

### 类型规则

- 前端 ID 一律使用 `string`
- 不要在 Vue SFC 的 `<script setup>` 中内联定义复杂 `type` 或 `interface`

## 常用命令

```bash
pnpm dev:admin
pnpm dev:site
pnpm build
pnpm lint
pnpm typecheck
pnpm typecheck:packages
pnpm layer:list
pnpm layer:add community
pnpm layer:add commerce
```

## 常见问题

### 页面能打开但接口全失败

先确认后端是否正常：

```bash
curl http://localhost:9999/mortise/actuator/health
```

### 管理端出现 CORS

本地开发时不要把管理端 API 基地址改成完整远端 URL，否则会绕过代理。

### 修改 packages 后页面没有更新

大多数情况下 workspace 会热更新；如果没有，重启对应 app 的 dev server。
*** Add File: e:\workspace\mortise\frontend\apps\site\content\docs\commercial-modules.md
---
title: 商业模块开发
description: 商业后端模块与前端 Layer 的边界、接入方式与自检清单
order: 5
---

# 商业模块开发

商业能力在 Mortise 中分成两类：后端业务子模块，以及前端可选 Layer。它们都不应该直接散落进主宿主工程。

## 后端商业模块边界

推荐结构：

```text
mortise-xxx/
├── mortise-xxx-domain/
├── mortise-xxx-infra/
├── mortise-xxx-application/
├── mortise-xxx-admin/
└── mortise-xxx-api/
```

职责约束：

- `domain` 放实体、枚举、纯业务模型
- `infra` 放 Mapper、Repository、第三方适配器
- `application` 放用例编排、事务、通知发起、事件监听
- `admin` / `api` 只做接口适配、参数绑定、权限和日志注解

### 不推荐的做法

- 直接改 `mortise-auth` 写业务路径授权
- 在 Controller 里直接发通知
- 业务模块直接依赖同层基础模块内部实现

## 后端扩展方式

### 安全规则

业务模块应在自己的 `api` 或 `admin` 模块里实现 `SecurityConfigurer`，注册本模块路径授权规则。

### 通知能力

- 使用已有通知能力时，在 `application` 层注入 `NotificationService`
- 新增通道时，实现 `NotificationSender`

### 日志与审计

- 查询接口优先使用 `@ApiLog`
- 变更接口补齐 `@ApiLog` + `@OperationLog`
- 对接外部审计系统时，实现 `LogStorage`

## 前端商业 Layer 边界

前端商业能力优先放在 `frontend/layers/<name>`，不要把页面直接散落到 `apps/site` 或 `apps/admin`。

推荐结构：

```text
frontend/layers/xxx/
├── package.json
├── nuxt.config.ts
├── README.md
├── app/
│   ├── pages/
│   ├── components/
│   ├── composables/
│   └── types/
└── server/
```

### 激活方式

```bash
cd frontend
pnpm layer:add community
pnpm layer:add commerce
```

### 前端约束

- 可选业务功能优先进入 Layer
- 跨 Layer / 跨 app 复用逻辑进入 `packages/*`
- 认证继续复用 `@mortise/auth`
- API 调用继续复用 `@mortise/core-sdk`

## 最小自检清单

### 后端

```bash
mvn -pl mortise-xxx-api -am clean compile -DskipTests
mvn -pl mortise-xxx-admin -am clean compile -DskipTests
```

### 前端

```bash
cd frontend
pnpm layer:add <name>
pnpm --filter @mortise/site typecheck
```

## 完成定义

- 模块职责没有混层
- 路由权限通过模块自己的扩展点注册完成
- 变更接口补齐日志策略
- 前端 Layer 没有重复实现宿主 app 已有鉴权和 SDK 逻辑
- 至少完成一次最小编译或类型校验
*** Add File: e:\workspace\mortise\frontend\apps\site\content\docs\troubleshooting.md
---
title: 开发排障
description: 本地开发最常见的 ENCRYPTION_KEY、Flyway 权限和代理问题处理方式
order: 6
---

# 开发排障

如果 Mortise 第一次在本机启动失败，优先按下面顺序排查，而不是直接改代码。

## 1. 先看 ENCRYPTION_KEY

当配置文件里存在 `ENC(...)` 时，当前 Shell 必须先设置 `ENCRYPTION_KEY`。否则会出现数据库、Redis 等核心配置为空的问题。

```powershell
$env:ENCRYPTION_KEY = "your_secret_key"
echo $env:ENCRYPTION_KEY
```

常见表现：

- `dataSource or DataSourceTransactionManager are required`
- `Failed to determine a suitable driver class`
- `password authentication failed`

## 2. Flyway 报 PostgreSQL 权限错误

典型错误：

```text
ERROR: permission denied for database postgres
ERROR: permission denied for schema mortise
```

优先执行根目录修复脚本：

```powershell
.\fix-postgresql-permissions.ps1
```

如果你使用数据库工具手工处理，至少要执行：

```sql
GRANT CREATE ON DATABASE postgres TO mortise;
CREATE SCHEMA IF NOT EXISTS mortise;
ALTER SCHEMA mortise OWNER TO mortise;
```

## 3. 管理端接口全红

先确认两件事：

- 后端监听地址仍是 `http://localhost:9999/mortise`
- 本地开发没有把管理端 API 基地址改成完整远端 URL

否则会绕过本地代理，直接触发 CORS。

## 4. 前端命令执行目录错误

下面这些命令都必须在 `frontend/` 目录执行：

```bash
pnpm install
pnpm dev:admin
pnpm dev:site
pnpm typecheck
```

## 5. 最小验证顺序

建议始终按这个顺序确认环境：

1. `http://localhost:9999/mortise/actuator/health`
2. `http://localhost:3000/admin/`
3. `http://localhost:3001/`

## 常用命令

```powershell
# 查看当前会话加密密钥
echo $env:ENCRYPTION_KEY

# 查看 9999 端口占用
Get-NetTCPConnection -LocalPort 9999 -ErrorAction SilentlyContinue

# 查看依赖服务状态
docker compose ps

# 查看 PostgreSQL 日志
docker compose logs postgresql
```
