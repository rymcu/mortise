# 商业子模块开发上手手册

这份文档现在作为总览入口页使用，详细内容已拆分为两份子文档：

1. [后端版](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md)
2. [前端 Layer 版](./COMMERCIAL_MODULE_FRONTEND_LAYER.md)

如果你只关心一个方向，直接进入对应文档即可。

## 如何选择

| 你的目标 | 应阅读文档 |
|------|------|
| 新增商业后端模块、接口、安全、通知、日志、监控 | [COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md) |
| 新增或维护前端商业 Layer、接入 `packages/auth` / `core-sdk` | [COMMERCIAL_MODULE_FRONTEND_LAYER.md](./COMMERCIAL_MODULE_FRONTEND_LAYER.md) |
| 需要前后端联调整体入口 | [QUICK_START.md](./QUICK_START.md) 和 [FRONTEND_QUICK_START.md](./FRONTEND_QUICK_START.md) |

## 5 分钟速查表

| 需求 | 推荐落点 | 对应文档 |
|------|----------|----------|
| 新增公开/鉴权/管理接口权限 | `api/admin` 模块实现 `SecurityConfigurer` | [后端版](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md) |
| 发站内信、邮件、微信通知 | `application` 注入 `NotificationService` | [后端版](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md) |
| 接入日志审计或外部日志系统 | `@ApiLog` / `@OperationLog` / `LogStorage` | [后端版](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md) |
| 上报业务指标或健康检查 | `MeterRegistry` / `HealthIndicator` | [后端版](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md) |
| 新增商业前端功能 | `frontend/layers/<name>` | [前端 Layer 版](./COMMERCIAL_MODULE_FRONTEND_LAYER.md) |
| 复用鉴权和 API SDK | `frontend/packages/auth`、`frontend/packages/core-sdk` | [前端 Layer 版](./COMMERCIAL_MODULE_FRONTEND_LAYER.md) |

## 推荐阅读

- [后端版](./COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md)
- [前端 Layer 版](./COMMERCIAL_MODULE_FRONTEND_LAYER.md)
- [QUICK_START.md](./QUICK_START.md)
- [FRONTEND_QUICK_START.md](./FRONTEND_QUICK_START.md)
- [module-dependency-and-spi-architecture.md](../module-dependency-and-spi-architecture.md)
