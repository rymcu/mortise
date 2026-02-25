# packages/config Plan

前端工程共享配置包。

## 责任范围

- ESLint 配置
- TypeScript 基础配置
- 主题与设计 token 配置
- 环境变量约定

## 迭代计划

1. 提供基础 `eslint` 与 `tsconfig` 共享配置。
2. 统一 Nuxt UI 主题色与暗黑模式策略。
3. 规范 `.env` 命名与变量校验。
4. 增加 CI 校验脚本（lint/typecheck）。

## 完成标准

- 多应用配置一致，不重复定义。
- 版本升级和规则调整可集中维护。
