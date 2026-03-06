# 官网（site）从零到完整实现

旧模板 `old-code/landing` 是 Nuxt UI 官方 landing 模板，可复用其组件结构（AppHeader / AppFooter / HeroBackground）并替换为 Mortise 实际内容。内容采用双轨：`@nuxt/content` 管理静态文档（Markdown），后端 Community REST API 驱动动态文章。

## Steps

1. **完善 `frontend/apps/site/nuxt.config.ts` 配置**
   - 添加 `@nuxt/content`、`@nuxt/image` 模块（参考旧模板 package.json）
   - 添加 vite proxy（`/mortise` → `http://localhost:9999`，与 admin 一致）
   - 保持 SSR: true（SEO 友好，官网需要 SEO）

2. **创建全局 Layout**
   - `app/layouts/default.vue`：顶部 `AppHeader` + `<slot>` + 底部 `AppFooter`
   - 从旧模板移植 `AppHeader.vue`（含滚动锚点侦听、主题切换按钮），替换为 Mortise 菜单项（首页 / 社区 / 文档 / 下载 / 关于）
   - 从旧模板移植 `AppFooter.vue`（3 列链接 + 社交媒体），替换为 Mortise 实际链接和 `AppLogo`
   - 创建 `AppLogo.vue`（基于 `UColorModeImage` 或 SVG，使用 `--ui-primary` 颜色变量）

3. **创建 Composables**
   - `app/composables/useSiteConfig.ts`：调用 `GET /mortise/api/v1/admin/system/site-config/public`，缓存站点名称、Logo、描述等全局配置
   - `app/composables/useArticles.ts`：封装 `GET /mortise/api/v1/community/articles`（分页、搜索过滤）和 `GET /mortise/api/v1/community/articles/{id}`

4. **创建首页 `app/pages/index.vue`**
   - 区块参考旧模板结构：Hero → 产品特性（6 个 Feature 卡片）→ 快速开始步骤（3 步）→ CTA（跳转 web 注册）
   - 使用 `@nuxt/content` 的 `queryCollection('home')` 从 `content/home.yml` 读取文案（标题、描述、功能点）
   - Hero 区包含"进入社区"（→ `/articles`）和"查看文档"（→ `/docs`）两个主按钮
   - 移植 `HeroBackground.vue`（渐变 SVG 背景动画）

5. **创建社区文章页**
   - `app/pages/articles/index.vue`：分页文章列表，顶部专题 `UBadge` 过滤、标签过滤，调用 `useArticles()`；卡片展示标题 / 摘要 / 作者 / 时间
   - `app/pages/articles/[id].vue`：文章详情，使用 `UPageHeader` 展示标题 / 作者信息，`ContentRenderer` 或自定义渲染 MD 内容，底部评论列表（`GET /community/articles/{id}/comments`）
   - 未登录用户只读；评论框提示“登录后发表评论” → 跳转至 `apps/site` 登录页

6. **创建文档中心（@nuxt/content 驱动）**
   - 在 `frontend/apps/site/` 下创建 `content/docs/` 目录，初始结构：`getting-started.md`、`architecture.md`、`api.md`（内容可从 `docs/` 目录迁移）
   - `app/pages/docs/index.vue`：文档导航首页
   - `app/pages/docs/[...slug].vue`：使用 `ContentDoc` 组件自动渲染 Markdown，左侧 `UNavigationMenu` 目录树
   - `content.config.ts`：配置 `docs` collection（`type: 'page'`，支持 frontmatter 的 `title`、`description`、`order`）

7. **创建下载页 `app/pages/download.vue`**
   - 展示客户端/SDK 下载卡片（内容来自 `content/download.yml`）
   - 每个卡片含平台图标（`simple-icons`）、版本号、下载按钮

8. **创建关于页 `app/pages/about.vue`**
   - 项目简介、团队介绍、开源协议（MIT）、GitHub 链接
   - 内容从 `content/about.yml` 读取

9. **配置 @nuxt/content**
   - `content.config.ts`：定义两个 collection：`home`（`type: 'data'`，单文件 YML）和 `docs`（`type: 'page'`，Markdown 目录）
   - 初始化 `content/home.yml` 填入 Mortise 实际品牌文案

10. **安装新依赖**
    - 在 `frontend/apps/site/package.json` 中添加：`@nuxt/content`、`@nuxt/image`、`@iconify-json/lucide`、`@iconify-json/simple-icons`
    - 运行 `pnpm install`（在 `frontend/` 目录下）

## Verification

- `pnpm --filter site dev` 本地启动，访问各路由确认页面渲染
- 确认 `/mortise/api/v1/community/articles` 请求通过 vite proxy 正常代理
- 确认 `/docs/getting-started` 路由能正确渲染 Markdown 内容
- `pnpm --filter site build` 确认 SSR 构建无报错

## Decisions

- 颜色主题沿用当前 green/zinc（已配置），不改为旧模板的 orange
- 文档内容复用 `docs/` 目录已有资料（架构文档等），迁移为 Markdown 放入 `content/docs/`
- SSR 保持开启（SEO 优先），与 admin（SPA）不同
