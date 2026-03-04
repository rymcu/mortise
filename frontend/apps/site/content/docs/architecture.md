---
title: 架构介绍
description: Mortise 项目的模块划分、技术栈与前后端架构
order: 2
---

# 架构介绍

Mortise 采用 **分层架构 + 多模块** 设计，25 个 Maven 模块、6 层依赖矩阵、12 组 SPI 可扩展接口。

## 后端模块划分

| 模块 | 说明 |
|------|------|
| `mortise-app` | 应用启动模块（入口） |
| `mortise-core` | 核心框架（异常、响应封装、全局常量） |
| `mortise-common` | 公共工具类 |
| `mortise-web-support` | Web 支持（过滤器、拦截器、Resilience4j 限流） |
| `mortise-auth` | 认证授权（Spring Security 6 + JWT + OAuth2 多平台） |
| `mortise-cache` | 缓存抽象（Redis，SPI 可扩展） |
| `mortise-persistence` | 数据访问（MyBatis-Flex 1.11.0） |
| `mortise-system` | 系统模块（用户、角色、菜单、字典） |
| `mortise-member` | 会员模块 |
| `mortise-community` | 社区模块（文章、评论） |
| `mortise-commerce` | 电商模块（商品、订单） |
| `mortise-payment` | 支付模块 |
| `mortise-notification` | 通知模块（邮件/微信模板消息） |
| `mortise-file` | 文件模块（x-file-storage） |
| `mortise-log` | 操作日志（`@OperationLog` AOP） |
| `mortise-monitor` | 监控告警（Prometheus + Actuator） |
| `mortise-wechat` | 微信生态（WxJava 多账号公众号管理） |

## 后端技术栈

| 层次 | 技术选型 |
|------|----------|
| Web 框架 | Spring Boot 3.5.7 |
| 安全认证 | Spring Security 6 + JWT (JJWT 0.12.5) |
| OAuth2 登录 | GitHub / Google / Logto / 微信多平台 |
| ORM | MyBatis-Flex 1.11.0 |
| 数据库 | PostgreSQL 17（推荐） / MySQL 8.0+ |
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

## 前端架构

前端采用 **pnpm Workspace Monorepo** 管理三个独立应用：

| 应用 | 路径 | 模式 | 端口 |
|------|------|------|------|
| admin - 管理端 | `apps/admin` | SPA | localhost:3000/admin/ |
| web - 用户端 | `apps/web` | SSR | localhost:3001/ |
| site - 官网 | `apps/site` | SSR | localhost:3103/ |

三端共享内部包：

- **`@mortise/auth`** — 统一鉴权包（Token 注入、401 自动刷新、70 单飞续期）
- **`@mortise/core-sdk`** — 后端 API 封装 SDK

## SPI 可扩展架构

| SPI 组 | 说明 |
|---------|------|
| `CacheConfigurer` | 模块化缓存 TTL 注册 |
| `CacheExpirationHandler` | Redis Key 过期事件处理 |
| `CustomUserDetailsService` | 多用户表认证（系统用户/会员用户分表） |
| `NotificationSender` | 多渠道通知扩展（短信/邮件/微信） |
| `LogStorageService` | 操作日志存储实现 |
| `OAuth2Strategy` | OAuth2 多平台登录扩展 |

## 认证流程

Mortise 支持四种登录方式：

1. **账号密码** — JWT 无状态 Token，支持自动续期
2. **手机验证码** — Redis 存储验证码、有效期全管理
3. **扫码登录** — Spring Event 事件驱动，支持微信公众号扫码
4. **OAuth2 多平台** — GitHub / Google / Logto / 微信，Strategy 模式可扩展


## 后端架构

### 模块划分

```
mortise/
├── mortise-app          # 应用启动模块（入口）
├── mortise-core         # 核心框架（异常、响应封装）
├── mortise-common       # 公共工具类
├── mortise-web-support  # Web 支持（过滤器、拦截器）
├── mortise-auth         # 认证授权（OAuth2 + Spring Security）
├── mortise-cache        # 缓存抽象（Redis）
├── mortise-persistence  # 数据访问（MyBatis-Flex）
├── mortise-system       # 系统模块（用户、角色、权限）
├── mortise-member       # 会员模块
├── mortise-community    # 社区模块（文章、评论）
├── mortise-commerce     # 电商模块（商品、订单）
├── mortise-payment      # 支付模块
├── mortise-notification # 通知模块（短信、邮件）
├── mortise-file         # 文件模块
├── mortise-log          # 操作日志
└── mortise-monitor      # 监控告警
```

### 技术栈

| 层次 | 技术选型 |
|------|----------|
| Web 框架 | Spring Boot 3.x |
| ORM | MyBatis-Flex |
| 数据库 | PostgreSQL 15+ |
| 缓存 | Redis 7+ |
| 认证 | Spring Authorization Server |
| 数据库迁移 | Flyway |
| API 文档 | Knife4j (OpenAPI 3) |
| 限流/熔断 | Resilience4j |

## 前端架构

前端采用 **pnpm monorepo** 管理三个独立应用：

- **web** - 用户端（SSR，Vue 3 + Nuxt 4）
- **admin** - 管理端（SPA，Vue 3 + Nuxt 4）
- **site** - 官网（SSR，Vue 3 + Nuxt 4）

三端共享 `@mortise/ui` 组件包和 `@mortise/auth` 认证包。

## API 设计规范

所有 API 遵循统一规范：

- 路径：`/mortise/api/v1/{模块}/{资源}`
- 响应格式：`{ code, message, data }`
- 分页参数：`pageNum`, `pageSize`
- 认证：Bearer Token (JWT)
