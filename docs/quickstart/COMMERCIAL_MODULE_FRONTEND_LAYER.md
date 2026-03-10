# 商业子模块开发上手手册（前端 Layer 版）

本手册面向商业前端功能的开发者，重点说明如何在 `frontend/layers/*` 中承载可选功能，并与 `apps/site`、`apps/admin` 及 `frontend/packages/*` 保持正确边界。

## 目录

- [5 分钟速查表](#5-分钟速查表)
- [1. 前端商业 Layer 的边界](#1-前端商业-layer-的边界)
- [2. 前端商业 Layer 怎么接](#2-前端商业-layer-怎么接)
- [3. 前端 Layer 脚手架清单](#3-前端-layer-脚手架清单)
- [4. 真实示例索引](#4-真实示例索引)
- [5. 完成定义](#5-完成定义)

## 5 分钟速查表

| 需求 | 推荐落点 | 不推荐做法 |
|------|----------|------------|
| 新增商业页面能力 | `frontend/layers/<name>` | 把页面散落到 `apps/site` |
| 跨 app 复用鉴权 | `frontend/packages/auth` | 每个页面自己写一套登录态逻辑 |
| 跨 app 复用 API 调用 | `frontend/packages/core-sdk` | 在多个页面重复写 fetch 封装 |
| Layer 挂载 | `pnpm layer:add <name>` | 手动散改多个 app 的依赖 |
| Layer 内业务组件 | `frontend/layers/<name>/app/components` | 直接改宿主 app 组件覆盖 |

---

## 1. 前端商业 Layer 的边界

前端商业功能不要直接散落在 `apps/site` 或 `apps/admin` 中。优先沿用 Layer 模式，把可选能力放在 `frontend/layers/<name>`。

推荐结构：

```text
frontend/
├── apps/
│   ├── admin/
│   └── site/
├── layers/
│   ├── base/
│   ├── community/
│   └── commerce/
└── packages/
    ├── auth/
    ├── core-sdk/
    ├── ui/
    └── config/
```

规则：

1. **可选商业功能优先放 `frontend/layers/<name>`**。
2. **跨 app 复用逻辑放 `frontend/packages/*`**。
3. **不要在页面里重复写鉴权和 SDK 封装**，优先复用 `@mortise/auth`、`@mortise/core-sdk`。
4. `apps/admin` 是管理端宿主，`apps/site` 是站点与用户端宿主，也是可选 Layer 的主挂载点。

---

## 2. 前端商业 Layer 怎么接

### 2.1 启用方式

```bash
cd frontend
pnpm layer:add community
pnpm layer:add commerce
```

该脚本会：

1. 检查 `layers/<name>` 是否存在且包含 `package.json` 与 `nuxt.config.ts`
2. 自动把 `@mortise/<name>-layer` 写入目标 app 的 `package.json`
3. 自动执行 `pnpm install`

### 2.2 本地开发约束

1. 本地开发命令统一在 `frontend/` 目录执行，并只使用 `pnpm`。
2. 管理端本地开发依赖相对路径 `/mortise` 和 Vite 代理，不要轻易改成完整远端 URL。
3. 如果 Layer 需要共享 TS 能力，把 composable、类型、请求封装优先放 `frontend/packages/*`，不要复制到多个 Layer。
4. Layer 页面不应直接复制宿主 app 里的鉴权逻辑。

### 2.3 与后端联调时要确认的事

1. 后端仍监听 `http://localhost:9999/mortise`
2. 管理端本地代理没有被完整远端 URL 绕过
3. Layer 页面仍通过统一 `/mortise` 或既有 SDK 调用

---

## 3. 前端 Layer 脚手架清单

### 3.1 新建前端商业 Layer 时

1. 在 `frontend/layers/<name>` 建立 Layer。
2. 通过 `pnpm layer:add <name>` 激活，不手动散改多个 app。
3. API 调用优先复用 `@mortise/core-sdk`。
4. 认证态优先复用 `@mortise/auth`。
5. 路由与页面结构由 Layer 承载，不把商业功能散落到宿主 app。
6. 能沉淀为跨 Layer 通用能力的，再考虑上提到 `frontend/packages/*`。

### 3.2 前端 Layer 目录模板

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

原则：

1. 页面、组件、主题配置属于 Layer 自身。
2. 通用鉴权和 SDK 请求不要重复封装，继续走 `@mortise/auth`、`@mortise/core-sdk`。
3. Layer 保持最小依赖面，只声明自己确实需要的包。

### 3.3 最小文件模板

`package.json`：

```json
{
  "name": "@mortise/xxx-layer",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "dependencies": {
    "@mortise/core-sdk": "workspace:*"
  }
}
```

`nuxt.config.ts`：

```ts
export default defineNuxtConfig({
  imports: {
    dirs: ['stores']
  }
})
```

### 3.4 首次自检

1. `cd frontend && pnpm install`
2. `pnpm layer:add <name>` 激活 Layer
3. `pnpm --filter @mortise/site dev` 或对应目标 app 的 `dev`
4. `pnpm --filter @mortise/site typecheck`
5. 检查路由是否按预期挂载，接口是否仍通过统一 `/mortise` 或既有 SDK 调用

### 3.5 常见反模式

1. **把商业前端页面散落到 `apps/site`**。
2. **在多个 Layer/页面里重复写 fetch 封装**。
3. **直接复制宿主 app 里的鉴权逻辑**。
4. **不通过 `pnpm layer:add`，手动到处改依赖**。

---

## 4. 真实示例索引

- [frontend/layers/community/README.md](../../frontend/layers/community/README.md)
- [frontend/layers/community/package.json](../../frontend/layers/community/package.json)
- [frontend/layers/community/nuxt.config.ts](../../frontend/layers/community/nuxt.config.ts)
- [frontend/scripts/layer.mjs](../../frontend/scripts/layer.mjs)
- [frontend/apps/site/package.json](../../frontend/apps/site/package.json)

---

## 5. 完成定义

一个新的商业前端 Layer 在进入联调或提测前，至少应满足下面这些条件：

1. 已通过 `pnpm layer:add <name>` 正常激活。
2. 页面、组件、类型、请求封装没有越过 `Layer / packages / host app` 的边界。
3. 没有重复实现宿主 app 已有的鉴权和 SDK 逻辑。
4. 已至少完成一次 `typecheck` 和一次本地联调验证。
