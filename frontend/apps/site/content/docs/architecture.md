---
title: 架构介绍
description: Mortise 项目的模块划分、技术栈与前后端架构
order: 2
---

# 架构介绍

Mortise 采用 **分层架构 + 多模块** 设计，25 个 Maven 模块、6 层依赖矩阵、15 组 SPI 可扩展接口。

后端模块分为两类：

- **主仓开源模块** — 包含在主仓库，MIT 协议开源，直接 `git clone` 即可使用。
- **商用模块** — 以 Git Submodule 挂载的私有仓库，需单独购买授权后拉取。

## 主仓开源模块

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
| `mortise-product` | 通用产品目录基础模块（免费，已内置主仓） |
| `mortise-notification` | 通知模块（邮件/微信模板消息） |
| `mortise-file` | 文件模块（x-file-storage） |
| `mortise-log` | 操作日志（`@OperationLog` AOP） |
| `mortise-monitor` | 监控告警（Prometheus + Actuator） |
| `mortise-wechat` | 微信生态（WxJava 多账号公众号管理） |
| `mortise-test-support` | 测试支持工具类 |

## 商用模块（Git Submodule）

商用模块以 Git Submodule 方式挂载，购买授权后通过以下命令拉取：

```bash
# 拉取全部商用模块（需所有模块权限）
git clone --recurse-submodules git@github.com:rymcu/mortise.git

# 仅初始化已购买的指定模块
git submodule update --init --recursive mortise-payment mortise-commerce
```

| 模块 | 说明 | 激活方式 |
|------|------|----------|
| `mortise-commerce` | 电商模块（商品、订单） | Maven `pro` Profile 自动激活 |
| `mortise-payment` | 支付模块（微信支付 / 支付宝） | Maven `pro` Profile 自动激活 |
| `mortise-community` | 社区模块（文章、评论） | Maven `pro` Profile 自动激活 |

> `pro` Profile 通过检测 `mortise-payment/pom.xml` 是否存在来自动激活，无需手动指定。未购买模块直接忽略，不影响主仓编译。

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

前端位于 `frontend/` 目录，采用 **pnpm Workspace Monorepo** 架构，基于 **Nuxt 4 + Nuxt UI 4.5 + Pinia + TypeScript 5** 构建。

### 应用（apps/）

| 应用 | 包名 | 路径 | 渲染模式 | 开发端口 | Docker 端口 |
|------|------|------|----------|----------|------------|
| 管理端 | `@mortise/admin` | `apps/admin` | SPA | `localhost:3000/admin/` | `3101/admin/` |
| 官网 + 用户端 | `@mortise/site` | `apps/site` | SSR | `localhost:3001/` | `3103/` |

### 共享包（packages/）

| 包名 | 路径 | 说明 |
|------|------|------|
| `@mortise/auth` | `packages/auth` | 统一鉴权：Token 注入、401 单飞刷新、Token 持久化存储 |
| `@mortise/core-sdk` | `packages/core-sdk` | 后端 API SDK（管理端/用户端接口封装，OSS 边界内） |
| `@mortise/ui` | `packages/ui` | 共享业务 UI 组件（基于 Nuxt UI 4.5 二次封装） |
| `@mortise/config` | `packages/config` | 共享工程配置（ESLint / TypeScript / Prettier 规则） |

### 前端技术栈

| 层次 | 技术选型 |
|------|----------|
| 框架 | Nuxt 4（基于 Vue 3） |
| 组件库 | Nuxt UI 4.5（TailwindCSS 4 + Reka UI） |
| 状态管理 | Pinia |
| 语言 | TypeScript 5 |
| 包管理 | pnpm 10+ Workspace Monorepo |
| 工具函数 | VueUse |

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
| `/app/auth/login` | 会员登录 |
| `/app/auth/register` | 会员注册 |
| `/app/auth/callback` | OAuth2 回调 |
| `/app/profile` | 个人中心 |

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



