# apps/web — 用户端 / 会员端

面向终端用户的 H5/Web 应用，提供会员认证与个人业务操作。

## 技术栈

- **Nuxt 4**（SPA 模式，`ssr: false`）
- **Nuxt UI 4.5**（TailwindCSS 4 + Reka UI）
- **Pinia** 状态管理
- **@mortise/auth** — 统一鉴权包

## 快速启动

```bash
# 在 frontend/ 目录下
pnpm dev:web
```

访问：http://localhost:3001/

## 已实现页面

| 路由 | 描述 | 状态 |
|------|------|------|
| `/app/auth/login` | 会员登录（账号密码 + OAuth2） | ✅ |
| `/app/auth/register` | 会员注册 | ✅ |
| `/app/auth/callback` | OAuth2 回调兑换 token | ✅ |
| `/app/profile` | 个人中心（资料查看） | ✅ |

## 鉴权流程

1. **密码登录**：`POST /api/v1/app/auth/login` → 建立 session
2. **OAuth2 登录**：获取 `/api/v1/app/oauth2/auth-url/{registrationId}` → 第三方跳转 → 回调至 `/app/auth/callback` → 兑换 token
3. **Token 续期**：401 触发单飞刷新；刷新失败清理 session 跳登录页
4. **注册**：`POST /api/v1/app/auth/register`

## 配置说明

| 变量 | 说明 | 默认值（开发） |
|------|------|------|
| `NUXT_PUBLIC_API_BASE` | 后端 API 基础地址 | `/mortise`（代理转发） |

开发时 Vite 代理自动将 `/mortise/**` 转发至 `http://localhost:9999`。

## 业务边界

- 不出现任何 admin/商业域页面耦合
- 仅通过 `@mortise/auth` 处理鉴权，不在页面层散落 token 逻辑
