---
title: 快速开始
description: 从依赖启动、加密配置到前后端联调的最短路径
order: 1
---

# 快速开始

本页覆盖本地开发最常用的启动顺序，目标是尽快跑通 Mortise 的后端、管理端和站点端联调。

## 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 21+ | 推荐 Eclipse Temurin 21 |
| **Maven** | 3.9+ | 后端统一使用系统安装的 `mvn` |
| **Node.js** | 20+ | 推荐 22.x LTS |
| **pnpm** | 10.29+ | 前端工作区唯一支持的包管理器 |
| **Docker** | 20.10+ | 推荐用于启动 PostgreSQL / Redis / Logto |
| **Docker Compose** | 2.0+ | 使用 `docker compose` 命令 |
| **Git** | 2.30+ | 商业模块场景需配置 SSH Key |

## 克隆项目

```bash
git clone https://github.com/rymcu/mortise.git
cd mortise
```

如需包含商业子模块：

```bash
git clone --recurse-submodules git@github.com:rymcu/mortise.git
```

已有仓库补拉子模块：

```bash
git submodule update --init --recursive
```

## 开发模式最短路径

### 只改后端

```powershell
# 终端 1：仓库根目录启动依赖
docker compose up -d postgresql redis

# 终端 2：设置当前会话加密密钥并启动后端
Set-Location mortise-app
$env:ENCRYPTION_KEY = "your_secret_key"
mvn spring-boot:run
```

最小验证：

```powershell
Invoke-RestMethod http://localhost:9999/mortise/actuator/health
```

### 前后端联调

```powershell
# 终端 1：仓库根目录启动依赖
docker compose up -d

# 终端 2：启动后端
Set-Location mortise-app
$env:ENCRYPTION_KEY = "your_secret_key"
mvn spring-boot:run

# 终端 3：启动管理端
Set-Location ..\frontend
pnpm install
pnpm dev:admin

# 终端 4：按需启动站点端
pnpm dev:site
```

默认访问地址：

| 进程 | 地址 | 说明 |
|------|------|------|
| 后端 API | `http://localhost:9999/mortise` | Spring Boot 应用 |
| 管理端 | `http://localhost:3000/admin/` | `pnpm dev:admin` |
| 站点端 | `http://localhost:3001/` | `pnpm dev:site` |

## 启动依赖服务

根目录 `compose.yaml` 只负责基础设施依赖，不会启动 Spring Boot 应用本身：

```bash
docker compose up -d
docker compose ps
```

服务地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| PostgreSQL | `localhost:5432` | 主数据库 |
| Redis | `localhost:6379` | 缓存服务 |
| Logto | `http://localhost:3001` | OIDC 服务 |
| Logto Admin | `http://localhost:3002` | 管理入口 |
| Nginx | `http://localhost:80` | 反向代理 |

## 设置加密密钥

项目使用 Jasypt 解密配置文件中的 `ENC(...)` 敏感配置。当前 Shell 启动后端前必须设置 `ENCRYPTION_KEY`：

```bash
# Linux / macOS
export ENCRYPTION_KEY=your_secret_key

# PowerShell
$env:ENCRYPTION_KEY = "your_secret_key"
```

常见未配置表现：

- `dataSource or DataSourceTransactionManager are required`
- `Failed to determine a suitable driver class`
- `password authentication failed`
- 其他因数据库、Redis 等配置为空导致的启动异常

## 启动后端

```bash
cd mortise-app
mvn spring-boot:run
```

## 启动前端

前端命令必须在 `frontend/` 目录执行，并且只能使用 `pnpm`：

```bash
cd frontend
pnpm install
pnpm dev:admin
pnpm dev:site
```

## 日常开发建议

| 你的任务 | 最少需要启动的进程 |
|------|--------------------|
| 只改后端接口 / Flyway / 权限逻辑 | PostgreSQL + Redis + 后端 |
| 只改管理端页面 | 后端 + `pnpm dev:admin` |
| 只改站点页面 | 后端 + `pnpm dev:site` |
| 完整联调 | PostgreSQL + Redis + 后端 + 对应前端应用 |

## 常见问题

### Flyway 报权限错误

Windows 下优先执行根目录脚本：

```powershell
.\fix-postgresql-permissions.ps1
```

### 后端启动报数据库或解密异常

优先确认当前会话中环境变量是否生效：

```bash
echo $ENCRYPTION_KEY          # Linux/macOS
echo $env:ENCRYPTION_KEY      # PowerShell
```

### 切换分支后子模块目录为空

```bash
git submodule update --init --recursive
```

### 管理端接口全失败

确认后端仍监听 `http://localhost:9999/mortise`，本地开发不要把管理端 API 基地址改成完整远端 URL。
