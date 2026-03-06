# apps/site — 官网 + 用户端
官网应用，同时承载品牌展示、产品介绍、文档中心，以及会员登录、注册、个人中心等用户端功能（已由 `apps/web` 合并过来）。

## 目标定位

- 品牌展示
- 产品介绍
- 文档与公告
- 下载与社区入口
- 会员认证与个人中心

## 技术栈

- **Nuxt 4**（SSR 模式）
- **Nuxt UI 4.5**（TailwindCSS 4 + Reka UI）
- **@nuxt/content** 内容驱动
- **Pinia** 状态管理
- **@mortise/auth** — 统一鉴权包

## 快速启动

```bash
# 在 frontend/ 目录下
pnpm dev:site
```

访问：http://localhost:3001/

## 已实现页面

| 路由 | 描述 |
|------|------|
| `/app/auth/login` | 会员登录（账号密码 + OAuth2） |
| `/app/auth/register` | 会员注册 |
| `/app/auth/callback` | OAuth2 回调兑换 token |
| `/app/profile` | 个人中心（资料查看） |
| `/docs` | 文档中心 |
| `/pricing` | 定价方案 |
| `/download` | 下载页 |
| `/about` | 关于我们 |
