# packages/auth Plan

统一鉴权能力包，供 `apps/admin` 与 `apps/web` 复用。

## 责任范围

- 会话状态管理（Pinia）
- 登录/登出动作
- OAuth2 回调兑换
- Token 注入与刷新
- 路由守卫辅助

## 模块设计

- `store/auth.ts`：会话状态与动作
- `services/auth-service.ts`：登录、回调、刷新 API 适配
- `plugins/auth-fetch.ts`：请求拦截、401 重试、单飞刷新
- `guards/require-auth.ts`：路由守卫工具

## 关键策略

1. **自主鉴权流程**：不依赖通用 auth 全家桶。
2. **双登录统一模型**：密码流和 OAuth2 流产出同一会话结构。
3. **单飞刷新**：并发请求只触发一次 refresh。
4. **安全兜底**：refresh 失败立即清理会话并回登录页。

## 完成标准

- admin/web 仅通过本包处理鉴权。
- 不直接在页面层散落 token 逻辑。
- 所有 401 处理路径可回归验证。
