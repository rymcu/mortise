# 架构说明

本项目采用多模块单体 + 业务域分层架构，强调模块边界清晰、依赖单向、接口统一。

## 分层概览

- L6 应用层：mortise-app
- L5 业务域 API：*-admin / *-api
- L4 业务域应用与基础设施：*-application / *-infra / *-domain
- L3 应用基础层：mortise-auth / mortise-web-support / mortise-monitor
- L2 基础设施层：mortise-log / mortise-cache / mortise-notification / mortise-persistence
- L1 核心层：mortise-common / mortise-core

## 模块职责

| 模块 | 职责 | 说明 |
| --- | --- | --- |
| mortise-common | 通用工具与异常 | 基础工具、常量、异常定义 |
| mortise-core | 核心抽象 | 统一返回、结果码、基础配置 |
| mortise-log | 日志与审计 | 操作日志、审计日志、SPI 接口 |
| mortise-cache | 缓存能力 | 缓存抽象与 Redis 实现 |
| mortise-notification | 通知能力 | 邮件、消息通知等 |
| mortise-persistence | 数据持久化 | MyBatis-Flex、JDBC、数据库驱动 |
| mortise-auth | 认证授权 | JWT、Security、OAuth2 SPI |
| mortise-web-support | Web 支撑 | 全局异常、OpenAPI、限流、Web 配置 |
| mortise-monitor | 监控与指标 | Actuator、Prometheus、指标扩展 |
| mortise-system-domain | 系统领域 | 实体、常量、领域模型 |
| mortise-system-application | 系统应用 | 业务服务、用例编排 |
| mortise-system-infra | 系统基础设施 | Mapper、SPI 实现、存储 |
| mortise-system-admin | 系统管理端 | 管理端接口与配置 |
| mortise-system-api | 系统公开 API | 对外接口 |
| mortise-member-domain | 会员领域 | 实体、领域模型 |
| mortise-member-application | 会员应用 | 业务服务、用例编排 |
| mortise-member-infra | 会员基础设施 | Mapper、存储实现 |
| mortise-member-admin | 会员管理端 | 管理端接口与配置 |
| mortise-member-api | 会员公开 API | 对外接口 |
| mortise-wechat | 微信集成 | 公众号/扫码/消息接入 |
| mortise-test-support | 测试支撑 | 共享测试配置与工具 |
| mortise-app | 应用装配 | 统一启动与模块装配 |

## 依赖矩阵（简化）

| 层级 | 允许依赖 |
| --- | --- |
| L6 应用层 | L1-L5 全部模块 |
| L5 业务域 API | L3-L4 与本域 domain/application/infra |
| L4 业务域应用与基础设施 | L1-L3 与本域 domain |
| L3 应用基础层 | L1-L2 |
| L2 基础设施层 | L1 |
| L1 核心层 | 无 |

## Web 规范

- 使用 `@AdminController` 标记管理端接口，自动添加 `/admin` 前缀。
- 使用 `@ApiController` 标记公开 API，自动添加 `/api` 前缀。
- OpenAPI 分组自动生成：`admin` 与 `api`。

## 开发约束

- 同层不互相依赖。
- 业务层不依赖 Web 相关类型。
- DTO 校验放在 API 层，领域模型保持纯净。
