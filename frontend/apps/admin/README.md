# apps/admin — 后台管理端

基于 `nuxt-ui-templates/dashboard` 定制，面向系统管理员的后台管理界面。

## 技术栈

- **Nuxt 4**（SPA 模式，`ssr: false`）
- **Nuxt UI 4.5**（TailwindCSS 4 + Reka UI）
- **Pinia** 状态管理
- **@mortise/auth** — 统一鉴权包
- **@mortise/core-sdk** — 后端 API SDK
- **TanStack Table** — 高性能数据表格
- **Unovis** — 数据可视化图表
- **Zod** — 表单校验
- **date-fns** — 日期处理

## 快速启动

```bash
# 在 frontend/ 目录下
pnpm dev:admin
```

访问：http://localhost:3000/admin/

## 已实现页面

| 路由 | 描述 | 状态 |
|------|------|------|
| `/admin/auth/login` | 账号密码登录 + OAuth2 按钮 | ✅ |
| `/admin/auth/callback` | OAuth2 回调兑换 token | ✅ |
| `/admin/auth/forgot-password` | 忘记密码 | ✅ |
| `/admin/dashboard` | 数据概览仪表盘 | ✅ |
| `/admin/members` | 会员列表 | ✅ |
| `/admin/systems/users` | 用户管理（CRUD） | ✅ |
| `/admin/systems/roles` | 角色管理 | ✅ |
| `/admin/systems/menus` | 菜单管理（树形） | ✅ |
| `/admin/systems/dictionaries` | 字典项管理 | ✅ |
| `/admin/systems/dict-types` | 字典类型管理 | ✅ |
| `/admin/systems/oauth2-clients` | OAuth2 客户端配置 | ✅ |
| `/admin/systems/notification-channels` | 通知渠道配置 | ✅ |
| `/admin/systems/wechat-accounts` | 微信公众号管理 | ✅ |
| `/admin/systems/site-config` | 站点全局配置 | ✅ |
| `/admin/systems/operation-logs` | 操作日志 | ✅ |
| `/admin/systems/api-logs` | API 访问日志 | ✅ |
| `/admin/systems/cache` | 缓存管理 | ✅ |
| `/admin/systems/files` | 文件管理 | ✅ |
| `/admin/monitor` | 系统监控（Actuator） | ✅ |
| `/admin/settings` | 个人设置（资料/通知/安全） | ✅ |
| `/admin/inbox` | 消息中心 | ✅ |
| `/admin/setup` | 初始化引导 | ✅ |

## 鉴权流程

1. **密码登录**：`POST /api/v1/admin/auth/login` → 写入 session
2. **OAuth2 登录**：跳转 `/oauth2/authorization/{registrationId}` → 回调至 `/admin/auth/callback` → 兑换 token
3. **Token 续期**：请求 401 时触发单飞刷新（`POST /api/v1/admin/auth/refresh-token`）；刷新失败则跳登录页
4. **登出**：清除 session，跳转登录页

## 配置说明

| 变量 | 说明 | 默认值（开发） |
|------|------|------|
| `NUXT_PUBLIC_API_BASE` | 后端 API 基础地址 | `/mortise`（代理转发） |

开发时 Vite 代理自动将 `/mortise/**` 转发至 `http://localhost:9999`，无需额外配置。

## 业务边界

- 不出现任何商业模块（commerce/order/payment）的菜单与路由
- 所有请求通过 `@mortise/core-sdk` + `@mortise/auth` 拦截器发起
