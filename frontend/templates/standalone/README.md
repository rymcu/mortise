# standalone 模板 — 独立部署模板

通用模板应用，将任意 Nuxt Layer 独立部署为根路径应用（无需官网）。

## 一键生成（推荐）

```bash
cd frontend
pnpm create:standalone
```

脚本会自动：

1. 扫描 `layers/` 下可用的业务 Layer（排除 base）
2. 询问需要部署的 Layer
3. 询问应用名称
4. 询问可选参数（端口、主题色、显示名称等）
5. 复制模板并自动写入 Layer 依赖、根路径配置与品牌信息

生成完成后执行 `pnpm install` 即可启动。

## 手动创建

```bash
# 1. 复制模板到新目录
cp -r templates/standalone apps/<your-app>

# 2. 修改 package.json
#    - name → "@mortise/<your-app>"
#    - dependencies 添加你的 Layer，如 "@mortise/community-layer": "workspace:*"

# 3. 修改 app/app.config.ts，覆盖 Layer 的 basePath
#    community: { basePath: '' }

# 4. 如需自定义开发端口，修改 nuxt.config.ts 的 devServer.port

# 5. 按需修改品牌信息
#    app/app.vue        → 应用名称、SEO
#    AppHeader.vue       → Logo、导航菜单
#    AppFooter.vue       → 版权信息

# 6. 安装依赖并启动
cd frontend
pnpm install
pnpm --filter @mortise/<your-app> dev
```

## 目录结构

```
standalone/
├── nuxt.config.ts              # ★ 自动发现 Layer + 根路径路由提升 + dev port
├── package.json                # ★ name + layer 依赖
├── tsconfig.json
├── eslint.config.mjs
└── app/
    ├── app.config.ts           # ★ basePath 覆盖 + 主题色
    ├── app.vue                 # ★ 应用名称 / SEO
    ├── assets/css/main.css
    ├── components/
    │   ├── AppHeader.vue       # ★ 品牌 Logo + 导航
    │   └── AppFooter.vue       # ★ 版权信息
    ├── layouts/
    │   ├── default.vue
    │   └── auth.vue
    ├── middleware/
    │   └── auth.ts
    ├── pages/auth/
    │   ├── login.vue
    │   ├── register.vue
    │   ├── forgot-password.vue
    │   └── callback.vue
    ├── plugins/
    │   ├── api.ts
    │   ├── sessionRestore.client.ts
    │   └── tokenRefresh.client.ts
    └── stores/
        └── auth.ts
```

标记 `★` 的文件是需要根据业务自定义的部分，其余文件通常无需修改。

## 示例：独立部署 community

```bash
cp -r templates/standalone apps/my-community
```

[apps/my-community/package.json](apps/my-community/package.json)：

```json
{
  "name": "@mortise/my-community",
  "dependencies": {
    "@mortise/base-layer": "workspace:*",
    "@mortise/community-layer": "workspace:*"
  }
}
```

[apps/my-community/app/app.config.ts](apps/my-community/app/app.config.ts)：

```typescript
export default defineAppConfig({
  community: { basePath: '' },
  ui: { colors: { primary: 'green', neutral: 'zinc' } }
})
```

## 示例：独立部署 commerce

```bash
cp -r templates/standalone apps/my-shop
```

```json
{
  "dependencies": {
    "@mortise/base-layer": "workspace:*",
    "@mortise/commerce-layer": "workspace:*"
  }
}
```

```typescript
// app/app.config.ts
export default defineAppConfig({
  commerce: { basePath: '' },
  ui: { colors: { primary: 'blue', neutral: 'slate' } }
})
```

## Layer 装载与根路径工作方式

- `nuxt.config.ts` 会复用 `scripts/layer-discovery.mjs`，根据当前应用 `package.json` 中已声明且已安装的 layer 依赖自动装载 Layer。
- 业务 Layer 的路由前缀会自动根据 layer 目录推导，例如 `community` 会把 `/community/*` 提升到 `/*`。
- `app.config.ts` 中对应 Layer 的 `basePath` 仍需显式覆盖为空字符串，确保组件和链接也走根路径。
