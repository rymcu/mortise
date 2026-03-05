---
title: 快速开始
description: 从环境准备到应用运行的完整流程
order: 1
---

# 快速开始

Mortise 是一款现代化、模块化的企业级全栈业务开发平台，基于 **Spring Boot 3.5.7** + **MyBatis-Flex 1.11.0** + **JDK 21** 构建，涵盖认证授权、电商交易、聚合支付、社区运营等核心业务域，前后端全栈，开箱即用。

## 环境要求

| 工具 | 版本要求 | 推荐版本 |
|------|----------|----------|
| **JDK** | 21+ | Eclipse Temurin 21 |
| **Maven** | 3.9+ | 3.9.x |
| **Docker** | 20.10+ | 用于启动依赖服务 |
| **Docker Compose** | 2.0+ | - |
| **Node.js** | 20+ | 22.x LTS |
| **pnpm** | 10+ | 10.29.x |
| **PostgreSQL** | 12+（提供 Docker） | PostgreSQL 17 |
| **Redis** | 6.0+（提供 Docker） | 7.x |

## 第一步：克隆项目

```bash
git clone https://github.com/rymcu/mortise.git
cd mortise
```

如需拉取商业子模块（需权限）：

```bash
git clone --recurse-submodules git@github.com:rymcu/mortise.git
```

## 第二步：启动依赖服务

根目录的 `compose.yaml` 启动**基础设施依赖**（PostgreSQL、Redis、Logto、Nginx），不含 Spring Boot 应用本身：

```bash
# 启动依赖（首次会下载镜像，需几分钟）
docker compose up -d

# 检查服务状态
docker compose ps
```

服务地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| PostgreSQL | `localhost:5432` | 主数据库 |
| Redis | `localhost:6379` | 缓存服务 |
| Logto (OIDC) | `http://localhost:3001` | OAuth2 身份证书服务 |
| Nginx | `http://localhost:80` | 反向代理 |

## 第三步：设置加密密钥

项目使用 **Jasypt** 加密配置文件中的数据库密码、Redis 密码等敏感信息。启动前**必须**设置 `ENCRYPTION_KEY` 环境变量，否则应用启动即报错。

```bash
# Linux / macOS
export ENCRYPTION_KEY=your_secret_key

# PowerShell
$env:ENCRYPTION_KEY = "your_secret_key"
```

> ⚠️ 未设置此变量将导致 `ENC(...)` 加密配置无法解密，常见错误表现：`dataSource required`、`password authentication failed` 等。

## 第四步：启动应用

```bash
# Maven 插件（开发推荐）
cd mortise-app
mvn spring-boot:run
```

验证启动：

```bash
curl http://localhost:9999/mortise/actuator/health
# 返回 {"status":"UP"} 表示启动成功
```

## 第五步：启动前端

```bash
cd frontend
pnpm install

# 管理端（localhost:3000/admin/）
pnpm dev:admin

# 用户端（localhost:3001/）
pnpm dev:web

# 官网（localhost:3002/）
pnpm dev:site
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:9999/mortise |
| Swagger UI | http://localhost:9999/mortise/swagger-ui/index.html |
| Actuator | http://localhost:9999/mortise/actuator |
| 管理端 | http://localhost:3000/admin/ |
| 用户端 | http://localhost:3001/ |

## 常见问题

**Q: 启动报 `dataSource required` / 数据库密码为空**

配置文件包含 `ENC(...)` 加密値，但 `ENCRYPTION_KEY` 环境变量未设置或不正确，导致 Jasypt 无法解密。先确认当前 Shell 中变量是否已生效：

```bash
echo $ENCRYPTION_KEY          # Linux/macOS
echo $env:ENCRYPTION_KEY      # PowerShell
```

**Q: 切换分支后子模块目录为空**

```bash
git submodule update --init --recursive
```

**Q: Docker 启动慢或镜像下载失败**

配置国内 Docker 镜像加速器，或者耐心等待首次镇像下载完成。
