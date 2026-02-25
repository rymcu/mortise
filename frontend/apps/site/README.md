# apps/site Plan (Reserved)

官网应用预留目录，当前不是首期交付范围。

## 目标定位

- 品牌展示
- 产品介绍
- 文档与公告
- 下载与社区入口

## 与用户端边界

- `apps/site` 不承载登录后业务流程。
- 登录后业务统一跳转到 `apps/web`。

## 启动条件

满足以下条件后再启动：

1. `apps/admin` 与 `apps/web` 完成 MVP。
2. 明确官网信息架构与内容来源（CMS/markdown）。
3. 确认 SEO 与部署域名策略。

## 初始技术建议

- 可基于 `old-code/landing` 模板二次开发。
- 与其余应用共享 `packages/ui` 与 `packages/config`。
