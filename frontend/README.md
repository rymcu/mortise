# Mortise 前端 Monorepo

基于 **Nuxt 4 + Nuxt UI 4.5 + Pinia** 构建的前端 Monorepo 工作区，使用 pnpm Workspace 管理多个应用与共享包。

## 目录结构

```
frontend/
├── apps/
│   ├── admin/          # 后台管理端（@mortise/admin）
│   ├── site/           # 官网 + 用户端（@mortise/site）
├── layers/
│   ├── base/           # 公共基础层（@mortise/base-layer，始终加载）
│   ├── community/      # 社区模块层（@mortise/community-layer，付费，submodule）
│   └── commerce/       # 商城模块层（@mortise/commerce-layer，付费，submodule）
├── packages/
│   ├── auth/           # 统一鉴权包（@mortise/auth）
│   ├── core-sdk/       # 后端 API SDK（@mortise/core-sdk）
│   ├── ui/             # 共享业务 UI 组件（@mortise/ui）
│   └── config/         # 共享工程配置（@mortise/config）
├── templates/
│   └── standalone/     # 独立部署模板（不参与默认 build/typecheck）
├── scripts/
│   ├── layer.mjs           # Layer 管理脚本（add/remove/list）
│   └── create-standalone.mjs  # 独立部署应用生成脚本
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

**本地开发无需任何配置**，两个应用均已内置可用的默认值（admin 通过 Vite proxy 转发，site 直连 `localhost:9999`）。

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

# 启动官网/用户端（访问 http://localhost:3103/）
pnpm dev:site
```

> **开发代理说明**：管理端和官网/用户端均配置了 Vite 代理，将 `/mortise/**` 请求自动转发至 `http://localhost:9999`，无跨域问题。

> **模板目录说明**：`templates/` 下的模板项目不属于默认 pnpm workspace 应用集合，因此不会参与 `pnpm build`、`pnpm lint`、`pnpm typecheck` 的常规扫描。

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

### 官网 + 用户端（apps/site）

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

# 启动官网/用户端开发服务器
pnpm dev:site

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

# 创建独立部署应用（交互式向导）
pnpm create:standalone

# Layer 管理（添加/移除付费模块）
pnpm layer:list                         # 列出已克隆的可用 Layer
pnpm layer:add community                # 向 site 添加 community layer
pnpm layer:add commerce                 # 向 site 添加 commerce layer
pnpm layer:remove community             # 从 site 移除 community layer
pnpm layer:add community --app my-shop  # 向指定应用添加 layer
```

---

## 付费 Layer 管理

Mortise 通过 **Nuxt Layer + Git Submodule** 机制提供可按需激活的付费功能模块，所有 Layer 统一存放在 `layers/` 目录。

### 接入 Layer（以 community 为例）

**1. 克隆私有 submodule**

```bash
cd frontend
git submodule add git@github.com:rymcu/mortise-community-frontend.git layers/community
```

> 需先购买并获得对应私有仓库的 SSH 访问权限。

**2. 激活到应用**

```bash
pnpm layer:add community
# 等价于：手动在 apps/site/package.json dependencies 中添加
# "@mortise/community-layer": "workspace:*"
# 然后 pnpm install
```

执行后重启开发服务器，访问 `http://localhost:3001/community` 即可。

**3. 停用 Layer**

```bash
pnpm layer:remove community
# 等价于：从 apps/site/package.json 删除依赖并 pnpm install
```

### Layer 自动加载原理

`apps/site/nuxt.config.ts` 在构建时扫描 `layers/` 目录，仅加载同时满足以下两个条件的 Layer：

- 在 `apps/site/package.json` 的 `dependencies` 中声明
- 已安装到 `node_modules`（即 `pnpm install` 已执行）

`base` layer 无条件加载。

### 可用 Layer

| Layer | 包名 | 功能 | 默认路由前缀 |
|-------|------|------|--------------|
| `community` | `@mortise/community-layer` | 社区、文章、话题 | `/community` |
| `commerce` | `@mortise/commerce-layer` | 商城、订单、支付 | `/commerce` |

---

## 包说明

### @mortise/auth

统一鉴权能力包，供 `apps/admin` 与 `apps/site` 复用：

- `src/client.ts`：登录、回调、刷新认证客户端
- `src/runtime.ts`：Nuxt 认证运行时工厂（API、token 刷新、会话恢复）
- `src/result.ts`：API 响应结果标准化
- `src/storage.ts`：Token 持久化存储
- `src/types.ts`：鉴权相关类型定义

应用侧原则：`packages/auth` 提供基础能力，`apps/*` 基于这些能力扩展自己的 cookie key、登录跳转、菜单恢复等业务逻辑。

### @mortise/core-sdk

后端 API SDK（仅 OSS 范围），供页面层调用：

- `src/admin.ts`：管理端 API（system/member/file/wechat）
- `src/auth.ts`：通用鉴权相关接口（当前用户、后台菜单等）
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
