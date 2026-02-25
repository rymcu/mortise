# packages/ui Plan

共享 UI 组件层，基于 Nuxt UI 4.5 二次封装。

## 责任范围

- 业务通用组件（表单、表格、状态、上传等）
- 统一交互反馈（toast、confirm、empty/error states）
- 主题 token 与组件风格收敛

## 设计原则

- 仅做薄封装，不重复造基础组件。
- 与业务域解耦，不直接依赖具体页面。
- 优先复用 dashboard 模板现有组件结构。

## 迭代计划

1. 抽取布局与导航共性组件。
2. 抽取 `DataTable`、`SearchForm`、`StatusBadge`。
3. 抽取 `AuthCard`、`OAuthProviderButton`、`UploadPanel`。
4. 建立组件使用规范和示例页。

## 完成标准

- admin/web 共享组件复用率稳定提升。
- 视觉规范与交互行为一致。
