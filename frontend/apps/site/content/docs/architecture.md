---
title: 架构介绍
description: Mortise 项目的模块划分、技术栈与前后端架构
order: 2
---

# 架构介绍

Mortise 采用 **分层架构 + 多模块** 设计，25 个 Maven 模块、6 层依赖矩阵、15 组 SPI 可扩展接口。

后端模块分为两类：

description: Mortise 的模块分层、前端工作区结构与 SPI 扩展方式
- **商用模块** — 以 Git Submodule 挂载的私有仓库，需单独购买授权后拉取。

## 主仓开源模块

| 模块 | 说明 |
Mortise 是一个 Spring Boot 多模块单体项目，前端则是独立的 Nuxt Monorepo。整体目标不是堆模块数量，而是把业务域、基础能力和装配层边界稳定下来。
| `mortise-app` | 应用启动模块（入口） |
## 后端分层
| `mortise-common` | 公共工具类 |
典型分层如下：

| 层级 | 代表模块 | 职责 |
|------|----------|------|
| 第 6 层 | `mortise-app` | 聚合启动、统一装配 |
| 第 5 层 | `mortise-system-admin`、`mortise-member-api` | 管理端 / 客户端接口适配 |
| 第 4 层 | `*-application`、`*-infra` | 应用编排、仓储、第三方适配 |
| 第 3 层 | `mortise-auth`、`mortise-web-support`、`mortise-monitor` | 应用基础层 |
| 第 2 层 | `mortise-log`、`mortise-cache`、`mortise-notification`、`mortise-persistence` | 基础设施层 |
| 第 1 层 | `mortise-common`、`mortise-core` | 核心公共能力 |

关键约束：同层模块不要直接硬依赖，优先通过 SPI、Spring 事件和应用装配协作。

## 主仓开源模块
| `mortise-cache` | 缓存抽象（Redis，SPI 可扩展） |
| `mortise-persistence` | 数据访问（MyBatis-Flex 1.11.0） |
| `mortise-system` | 系统模块（用户、角色、菜单、字典） |
| `mortise-member` | 会员模块 |
| `mortise-notification` | 通知模块（邮件/微信模板消息） |
| `mortise-core` | 核心框架（异常、响应封装、全局常量） |
| `mortise-auth` | 认证授权（Spring Security 6 + JWT + OAuth2） |
| `mortise-web-support` | Web 支撑、限流、健康检查扩展 |
| `mortise-monitor` | Actuator、Prometheus、健康指标 |
| `mortise-file` | 文件模块（x-file-storage） |
| `mortise-monitor` | 监控告警（Prometheus + Actuator） |
| `mortise-log` | 接口审计与操作日志 |
| `mortise-notification` | 通知抽象与多通道发送 |
| `mortise-wechat` | 微信生态（WxJava 多账号公众号管理） |
| `mortise-test-support` | 测试支持工具类 |

商用模块以 Git Submodule 方式挂载，购买授权后通过以下命令拉取：
| `mortise-product` | 通用产品目录基础模块 |
# 拉取全部商用模块（需所有模块权限）
git clone --recurse-submodules git@github.com:rymcu/mortise.git

## 商业模块
git submodule update --init --recursive mortise-payment mortise-commerce
商业模块通过 Git Submodule 挂载，按需初始化：

| 模块 | 说明 | 激活方式 |

> `pro` Profile 通过检测 `mortise-payment/pom.xml` 是否存在来自动激活，无需手动指定。未购买模块直接忽略，不影响主仓编译。

| 模块 | 说明 |
|------|------|
| `mortise-commerce` | 电商模块（商品、订单、交易流程） |
| `mortise-payment` | 支付模块（微信支付 / 支付宝等） |
| `mortise-community` | 社区模块（文章、评论、互动） |
| 安全认证 | Spring Security 6 + JWT (JJWT 0.12.5) |
## 前端工作区

前端位于 `frontend/`，采用 pnpm workspace 管理多个 app、共享包和可选 Layer。
| ORM | MyBatis-Flex 1.11.0 |
### 应用（apps）

| 应用 | 包名 | 路径 | 渲染模式 | 本地端口 |
|------|------|------|----------|----------|
| 管理端 | `@mortise/admin` | `frontend/apps/admin` | SPA | `3000` |
| 官网 + 用户端 | `@mortise/site` | `frontend/apps/site` | SSR | `3001` |

### 共享包（packages）

| 包名 | 说明 |
|------|------|
| `@mortise/auth` | 登录、Token 注入、401 刷新、会话恢复 |
| `@mortise/core-sdk` | 共享 API SDK，避免页面层直接拼 URL |
| `@mortise/ui` | 共享业务 UI 组件 |
| `@mortise/config` | 工程配置（ESLint / TS / Prettier） |

### Layer（layers）

| Layer | 说明 |
|------|------|
| `base` | 常驻基础层 |
| `community` | 可选社区前端能力 |
| `commerce` | 可选商城前端能力 |

规则很简单：跨 app 复用逻辑放 `packages/*`，可选业务能力优先放 `layers/*`，宿主 app 只负责页面与组合。

## 核心技术栈
| 缓存 | Redis 6.0+ |
| 数据库迁移 | Flyway |
| 限流/熔断 | Resilience4j 2.2.0 |
| 配置加密 | Jasypt 3.0.5 |
| 监控 | Prometheus + Micrometer + Grafana |
| 文件存储 | x-file-storage + 阿里云 OSS |
| 微信集成 | WxJava |
| API 文档 | SpringDoc OpenAPI 2.8 (Swagger UI) |
| 分布式 ID | ULID Creator |
| 异步执行 | JDK 21 虚拟线程 |

### 已实现页面一览

**管理端（apps/admin）**

| 路由 | 说明 |
|------|------|
| `/admin/auth/login` | 管理员登录（账号密码 + OAuth2） |
| `/admin/auth/callback` | OAuth2 回调 |
| `/admin/auth/forgot-password` | 忘记密码 |
| `/admin/dashboard` | 数据仪表盘 |
| `/admin/members` | 会员列表 |
| `/admin/systems/users` | 用户管理 |
| `/admin/systems/roles` | 角色管理 |
| `/admin/systems/menus` | 菜单管理 |
| `/admin/systems/dictionaries` | 字典管理 |
| `/admin/systems/dict-types` | 字典类型管理 |
| `/admin/systems/oauth2-clients` | OAuth2 客户端配置 |
| `/admin/systems/notification-channels` | 通知渠道配置 |
| `/admin/systems/wechat-accounts` | 微信公众号管理 |
| `/admin/systems/site-config` | 站点配置 |
| `/admin/settings` | 个人设置（资料 / 通知 / 安全） |
| `/admin/inbox` | 消息中心 |
| `/admin/setup` | 初始引导 |

**官网 + 用户端（apps/site）**

| 路由 | 说明 |
|------|------|
| `/auth/login` | 会员登录 |
| `/auth/register` | 会员注册 |
| `/auth/callback` | OAuth2 回调处理 |
| `/auth/forgot-password` | 找回密码 |
| `/profile` | 个人中心 |
| `/docs` | 文档中心 |

## SPI 可扩展架构

常见扩展点：

| SPI | 作用 |
|-----|------|
| `SecurityConfigurer` | 模块自行注册路径鉴权规则 |
| `NotificationSender` | 扩展通知发送通道 |
| `LogStorage` | 扩展日志落库 / 外部存储 |
| `CacheConfigurer` | 注册缓存 key 与 TTL |
| `CacheExpirationHandler` | 响应缓存过期事件 |
| `CustomUserDetailsService` | 多用户体系认证扩展 |

以安全为例，`mortise-auth` 提供 SPI 契约，各业务模块在自己的 `api` 或 `admin` 模块实现 `SecurityConfigurer`，而不是直接改统一规则或让同层模块彼此硬依赖。

## 认证与接口边界

当前前端站点 `apps/site` 运行时配置使用：

| 类型 | 前端页面路由 | 后端接口前缀 |
|------|--------------|--------------|
| 会员认证 | `/auth/*` | `/api/v1/app/auth/*` |
| OAuth2 | `/auth/callback` | `/api/v1/app/oauth2/*` |
| 会员文件上传 | 页面内调用 | `/api/v1/app/files` |
| 站点公开配置 | 启动时加载 | `/api/v1/admin/system/site-config/public` |

这也是站点文档中 API 参考页统一采用 `/api/v1/app/*` 的原因。



