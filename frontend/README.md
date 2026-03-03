# Mortise 前端 Monorepo

基于 **Nuxt 4 + Nuxt UI 4.5 + Pinia** 构建的前端 Monorepo 工作区，使用 pnpm Workspace 管理多个应用与共享包。

## 目录结构

```
frontend/
├── apps/
│   ├── admin/          # 后台管理端（@mortise/admin）
│   ├── web/            # 用户端/会员端（@mortise/web）
│   └── site/           # 官网（@mortise/site，预留）
├── packages/
│   ├── auth/           # 统一鉴权包（@mortise/auth）
│   ├── core-sdk/       # 后端 API SDK（@mortise/core-sdk）
│   ├── ui/             # 共享业务 UI 组件（@mortise/ui）
│   └── config/         # 共享工程配置（@mortise/config）
├── package.json        # Monorepo 根配置
└── pnpm-workspace.yaml # pnpm 工作区声明
```

## 快速上手

### 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | 20+ | 推荐 22.x LTS |
| pnpm | 10+ | 推荐 10.29.x，不支持 npm/yarn |
| 后端服务 | - | 默认监听 `http://localhost:9999/mortise` |

### 1. 安装依赖

在 `frontend/` 目录下执行：

```bash
pnpm install
```

> pnpm 会自动安装所有 apps 和 packages 的依赖，无需分别进入子目录。

### 2. 配置环境变量

**本地开发无需任何配置**，两个应用均已内置可用的默认值（admin 通过 Vite proxy 转发，web 直连 `localhost:9999`）。

仅当后端不在 `localhost:9999` 时，才需要创建 `frontend/.env.local` 并覆盖：

```dotenv
# 仅在连接非 localhost 后端时设置（如测试/生产环境）
NUXT_PUBLIC_API_BASE=https://api.your-domain.com/mortise
```

> ⚠️ admin 的 Vite proxy 仅对相对路径生效，若设为完整 URL 会绕过代理导致本地 CORS 错误。

### 3. 启动开发环境

```bash
# 启动管理端（访问 http://localhost:3000/admin/）
pnpm dev:admin

# 启动用户端（访问 http://localhost:3001/）
pnpm dev:web
```

> **开发代理说明**：管理端和用户端均配置了 Vite 代理，将 `/mortise/**` 请求自动转发至 `http://localhost:9999`，无跨域问题。

### 4. 登录管理端

默认账号请参考后端文档（`/api/v1/admin/auth/login`），通常为初始化数据库时写入的管理员账号。

---

## 已实现页面

### 管理端（apps/admin）

| 路由 | 描述 |
|------|------|
| `/admin/auth/login` | 管理员登录（账号密码 + OAuth2 按钮） |
| `/admin/auth/callback` | OAuth2 回调页 |
| `/admin/auth/forgot-password` | 忘记密码 |
| `/admin/dashboard` | 数据仪表盘 |
| `/admin/members` | 会员列表 |
| `/admin/systems/users` | 用户管理 |
| `/admin/systems/roles` | 角色管理 |
| `/admin/systems/menus` | 菜单管理 |
| `/admin/systems/dictionaries` | 字典管理 |
| `/admin/systems/dict-types` | 字典类型管理 |
| `/admin/systems/oauth2-clients` | OAuth2 客户端配置 |
| `/admin/systems/notification-channels` | 通知渠道配置 |
| `/admin/systems/wechat-accounts` | 微信公众号管理 |
| `/admin/systems/site-config` | 站点配置 |
| `/admin/settings` | 个人设置（资料/通知/安全） |
| `/admin/inbox` | 消息中心 |
| `/admin/setup` | 初始引导 |

### 用户端（apps/web）

| 路由 | 描述 |
|------|------|
| `/app/auth/login` | 会员登录 |
| `/app/auth/register` | 会员注册 |
| `/app/auth/callback` | OAuth2 回调页 |
| `/app/profile` | 个人中心 |

---

## 常用命令

```bash
# 启动管理端开发服务器
pnpm dev:admin

# 启动用户端开发服务器
pnpm dev:web

# 构建所有应用
pnpm build

# ESLint 校验（所有 app）
pnpm lint

# Prettier 格式化
pnpm format

# Prettier 格式检查（不修改）
pnpm format:check

# ESLint + Prettier 联合检查（推荐 CI 使用）
pnpm lint:prettier-check

# TypeScript 类型检查（所有 app）
pnpm typecheck

# TypeScript 类型检查（所有 packages）
pnpm typecheck:packages
```

---

## 包说明

### @mortise/auth

统一鉴权能力包，供 `apps/admin` 与 `apps/web` 复用：

- `src/client.ts`：请求客户端，内置 Token 注入与 401 单飞刷新
- `src/result.ts`：API 响应结果标准化
- `src/storage.ts`：Token 持久化存储
- `src/types.ts`：鉴权相关类型定义

### @mortise/core-sdk

后端 API SDK（仅 OSS 范围），供页面层调用：

- `src/admin.ts`：管理端 API（system/member/file/wechat）
- `src/types.ts`：DTO/VO 类型定义
- 约束：禁止引入 commerce/order/payment 相关接口

### @mortise/ui

共享业务 UI 组件层，基于 Nuxt UI 4.5 二次封装。

### @mortise/config

共享工程配置（ESLint/TypeScript/Prettier 规则）。

---

## Docker Compose 部署

### 启动

在 `frontend/` 目录执行：

```bash
docker compose -f compose.yaml up -d --build
```

### 访问地址

| 应用 | 地址 |
|------|------|
| 管理端 | http://localhost:3101/admin/ |
| 用户端 | http://localhost:3102/ |
| 官网 | http://localhost:3103/ |

### 自定义后端地址

```bash
# Linux/macOS
NUXT_PUBLIC_API_BASE=https://your-domain/mortise docker compose -f compose.yaml up -d --build

# PowerShell
$env:NUXT_PUBLIC_API_BASE = "https://your-domain/mortise"
docker compose -f compose.yaml up -d --build
```

### 停止

```bash
docker compose -f compose.yaml down
```

---

## 开发说明

- 业务边界：严格遵循 OSS 边界，`core-sdk` 禁止引入商业模块接口。
- 路由守卫：所有需要登录的页面已通过 `auth` middleware 保护，401 自动触发单飞刷新，失败则跳转登录页。
- 密码输入：所有 `type="password"` 的 `UInput` 必须实现显示/隐藏切换，见 `.github/copilot-instructions.md`。
- 技术文档：详细的前端上手指南见 [docs/quickstart/FRONTEND_QUICK_START.md](../docs/quickstart/FRONTEND_QUICK_START.md)。
