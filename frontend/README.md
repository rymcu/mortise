# Frontend Workspace Plan

本目录用于承载 Mortise 前端 Monorepo（Nuxt 4 + Nuxt UI 4.5 + Pinia）。

## 目标边界（OSS）

仅覆盖主仓库开源能力：

- auth
- system
- member
- file
- wechat

不包含商品、订单、支付及其他商业子模块。

## 目录规划

- `apps/admin`：后台管理端（基于 dashboard 模板）
- `apps/web`：用户端（会员端/H5）
- `apps/site`：官网（预留）
- `packages/auth`：统一鉴权状态机与登录流程
- `packages/core-sdk`：后端 API SDK（仅 OSS API）
- `packages/ui`：共享业务 UI 封装
- `packages/config`：共享工程配置（eslint/ts/theme）

## 总体实施阶段

1. **初始化阶段**：建立 Monorepo、统一依赖版本、基础脚手架。
2. **鉴权阶段**：完成账号密码登录、OAuth2 state 回调兑换、刷新令牌单飞。
3. **管理端阶段**：交付系统管理核心页面（用户/角色/菜单/字典/OAuth2客户端）。
4. **用户端阶段**：交付会员登录注册、个人中心、文件与微信登录链路。
5. **工程化阶段**：完善监控、构建策略、能力开关与 CI 校验。

## 开发约束

- 严格按 capability 注册路由和菜单。
- `core-sdk` 禁止引用 commerce/order/payment API。
- 鉴权主流程自主实现，避免依赖通用 auth 全家桶。
