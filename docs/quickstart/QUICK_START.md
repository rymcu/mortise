# 🚀 Mortise 快速开始

本指南覆盖从环境准备到应用运行的完整流程，包括：

- 本地开发环境搭建
- 前后端联调的最短开发路径
- Docker Compose 一键启动依赖
- 敏感配置加密（Jasypt）
- 商业模块的按需拉取与维护

---

## 1. 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 21+ | 推荐 Eclipse Temurin 21 |
| **Maven** | 3.9+ | `mvn -version` 验证 |
| **Node.js** | 20+ | 前端开发必需，推荐 22.x LTS |
| **pnpm** | 10.29+ | 前端工作区唯一支持的包管理器 |
| **Docker** | 20.10+ | 可选，推荐用于启动依赖服务 |
| **Docker Compose** | 2.0+ | `docker compose version` 验证 |
| **Git** | 2.30+ | SSH Key 需配置好 |

---

## 2. 克隆项目

### 2.1 基础版（不含商业模块）

适合只使用开源部分的场景：

```bash
git clone https://github.com/rymcu/mortise.git
cd mortise
```

### 2.2 商业版（含私有子模块）

需要已获得对应私有仓库权限并配置好 SSH Key：

```bash
git clone --recurse-submodules git@github.com:rymcu/mortise.git
cd mortise
```

### 2.3 已有仓库补拉子模块

```bash
git submodule update --init --recursive
```

### 2.4 仅拉取部分商业模块

不要全量初始化，按需指定模块：

```bash
git clone git@github.com:rymcu/mortise.git
cd mortise

# 示例：仅拉取 payment + commerce
git submodule update --init --recursive mortise-payment mortise-commerce
```

停用本地不需要的模块：

```bash
# 示例：停用 product
git submodule deinit -f mortise-product
```

> 若账号无某模块权限，初始化时会报 `repository not found` 或 `Permission denied`，属预期行为。

---

## 3. 开发模式最短路径

如果你的目标是尽快进入本地开发，按下面顺序执行即可。

### 3.1 后端开发最短路径

适合只修改 Java / SQL / Flyway / 后端接口：

```powershell
# 1. 在仓库根目录启动依赖
docker compose up -d postgresql redis

# 2. 设置当前 PowerShell 会话的加密密钥
$env:ENCRYPTION_KEY = "your_secret_key"

# 3. 启动后端
Set-Location mortise-app
mvn spring-boot:run
```

最小验证：

```powershell
Invoke-RestMethod http://localhost:9999/mortise/actuator/health
```

### 3.2 全栈联调最短路径

适合同时修改后端和前端页面：

```powershell
# 终端 1：仓库根目录启动依赖
docker compose up -d

# 终端 2：启动后端
Set-Location mortise-app
$env:ENCRYPTION_KEY = "your_secret_key"
mvn spring-boot:run

# 终端 3：启动管理端前端
Set-Location frontend
pnpm install
pnpm dev:admin

# 终端 4：按需启动站点前端
Set-Location frontend
pnpm dev:site
```

启动成功后默认访问地址：

| 进程 | 地址 | 说明 |
|------|------|------|
| 后端 API | `http://localhost:9999/mortise` | Spring Boot 应用 |
| 管理端 | `http://localhost:3000/admin/` | `pnpm dev:admin` |
| 站点端 | `http://localhost:3001/` | `pnpm dev:site` |

### 3.3 日常开发建议

| 你的任务 | 最少需要启动的进程 |
|----------|--------------------|
| 只改后端接口 / Flyway / 权限逻辑 | PostgreSQL + Redis + 后端 |
| 只改管理端页面 | 后端 + `pnpm dev:admin` |
| 只改站点页面 | 后端 + `pnpm dev:site` |
| 做完整联调 | PostgreSQL + Redis + 后端 + 对应前端应用 |

说明：

1. 根目录 `compose.yaml` 只负责基础设施依赖，不会启动 Spring Boot 应用。
2. 后端统一使用系统安装的 `mvn`，不要使用 `mvnw` 或 `mvnw.cmd`。
3. 前端必须在 `frontend/` 目录使用 `pnpm`，不要使用 npm / yarn。
4. 管理端本地开发默认走代理，请保持后端监听 `localhost:9999` 以避免额外 CORS 配置。

### 3.4 首次开发排障清单

如果你是第一次在本机启动 Mortise，优先按这个顺序排查：

1. **后端没起来先看 `ENCRYPTION_KEY`**：当前 Shell 必须先设置环境变量，否则 `ENC(...)` 配置无法解密。
2. **Flyway 报数据库或 schema 权限错误先修权限**：优先执行仓库根目录的 `fix-postgresql-permissions.ps1`；如果本机没有 `psql` 客户端，直接按 `docs\database\FLYWAY_PERMISSION_FIX.md` 中的 GUI 手动 SQL 方案处理。
3. **管理端接口全红先确认后端地址**：本地开发默认依赖 `http://localhost:9999/mortise`，不要把管理端 API 基地址改成远端完整 URL。
4. **前端命令必须在 `frontend/` 目录执行**：`pnpm install`、`pnpm dev:admin`、`pnpm dev:site` 都只在该目录运行。
5. **根目录 `compose.yaml` 不启动 Java 应用**：`docker compose up -d` 之后还需要单独执行 `mvn spring-boot:run`。
6. **最小验证顺序**：先通 `actuator/health`，再打开管理端登录页，最后再看业务页面。

常用快速命令：

```powershell
# 检查当前 PowerShell 会话的加密密钥
echo $env:ENCRYPTION_KEY

# 修复 PostgreSQL 权限
.\fix-postgresql-permissions.ps1

# 检查后端健康状态
Invoke-RestMethod http://localhost:9999/mortise/actuator/health
```

### 3.5 Windows 开发者速查

仓库默认终端是 PowerShell，Windows 下优先使用下面这些命令：

```powershell
# 设置当前会话的 Jasypt 密钥
$env:ENCRYPTION_KEY = "your_secret_key"

# 查看环境变量是否已生效
echo $env:ENCRYPTION_KEY

# 启动后端
Set-Location mortise-app
mvn spring-boot:run

# 启动管理端
Set-Location ..\frontend
pnpm dev:admin

# 启动站点端
pnpm dev:site
```

常用排障命令：

```powershell
# 查看 9999 端口占用
Get-NetTCPConnection -LocalPort 9999 -ErrorAction SilentlyContinue

# 查看 Docker 服务状态
docker compose ps

# 查看 PostgreSQL 日志
docker compose logs postgresql

# 修复 PostgreSQL 权限
.\fix-postgresql-permissions.ps1
```

说明：

1. PowerShell 中 `$env:变量名 = "值"` 只对当前终端会话生效。
2. 如果你是从 IDE 启动后端，需要在 IDE 的运行配置里单独设置 `ENCRYPTION_KEY`。
3. 若 `Get-NetTCPConnection` 不可用，可退回使用 `netstat -ano | findstr 9999`。

---

## 4. 启动依赖服务

根目录的 `compose.yaml` 启动的是**基础设施依赖**（PostgreSQL、Redis、Logto、Nginx），**不含** Spring Boot 应用本身。

```bash
# 启动依赖（首次会拉取镜像，需几分钟）
docker compose up -d

# 验证服务状态
docker compose ps

# 查看日志
docker compose logs -f postgresql
```

启动后各服务地址：

| 服务 | 地址 | 默认凭据 |
|------|------|----------|
| PostgreSQL | `localhost:5432` | 见 `.env` 文件 |
| Redis | `localhost:6379` | 见 `.env` 文件 |
| Logto（OIDC） | `http://localhost:3001` | - |
| Logto（Admin） | `http://localhost:3002` | - |
| Nginx | `http://localhost:80` | - |

---

## 5. 配置应用

### 5.1 配置文件说明

| 文件 | 用途 |
|------|------|
| `mortise-app/src/main/resources/application.yml` | 指定激活的 profile（Maven 注入） |
| `mortise-app/src/main/resources/application-dev.yml` | 开发环境配置（数据库、Redis、邮件等） |
| `mortise-app/src/main/resources/application-prod.yml` | 生产环境配置 |

### 5.2 设置加密密钥环境变量

项目使用 Jasypt 加密配置文件中的敏感信息（数据库密码、邮件密码、OSS Key 等），**启动前必须设置 `ENCRYPTION_KEY` 环境变量**。

> ❌ **若未设置 `ENCRYPTION_KEY`**，配置文件中的 `ENC(...)` 值将无法解密，导致对应属性为空（`null` 或空字符串），应用启动即报错，常见表现：
> - `dataSource or DataSourceTransactionManager are required`
> - `Failed to determine a suitable driver class`
> - `Connection refused` / `password authentication failed`
> - 其他因关键配置项为空引发的初始化异常

```bash
# Linux / macOS
export ENCRYPTION_KEY=your_secret_key

# PowerShell
$env:ENCRYPTION_KEY = "your_secret_key"

# Windows CMD
set ENCRYPTION_KEY=your_secret_key
```

> ⚠️ `ENCRYPTION_KEY` 的值即为加密时使用的主密钥，需与生成 `ENC(...)` 密文时一致，**不要提交到版本库**。

---

## 6. Jasypt 敏感配置加密

### 6.1 原理

Spring Boot 启动时，Jasypt 自动解密配置文件中形如 `ENC(密文)` 的值，解密密钥来自环境变量 `ENCRYPTION_KEY`。

```yaml
# application-dev.yml 示例
spring:
  datasource:
    password: ENC(/T9oN1+Zyq6ZOYV4oOyJJFblmrbhla0tmI1ExpjXA/4cg1gw+Yh6kw==)
  data:
    redis:
      password: ENC(EBe9Le3JmVqg5iEvu9jMGTg33rBDoX5qocVTkrjyKyTU8XcH7ht2aQ==)
```

算法：`PBEWithMD5AndDES`，输出格式：Base64。

### 6.2 生成密文

**推荐方式**：使用项目内置的 `JasyptUtils` 工具类，先设置 `ENCRYPTION_KEY` 环境变量，然后在任意测试类或 main 方法中调用 `encryptPassword()`（自动读取环境变量中的主密钥）：

```java
// 前提：已设置 ENCRYPTION_KEY 环境变量
// encryptPassword 会自动读取 System.getenv("ENCRYPTION_KEY") 作为主密钥
String cipher = JasyptUtils.encryptPassword("your_plain_password");
System.out.println("ENC(" + cipher + ")");
```

若需在没有环境变量的情况下手动传入密钥，可使用重载方法：

```java
String cipher = JasyptUtils.encryptJasyptPassword("your_plain_password", "your_secret_key");
System.out.println("ENC(" + cipher + ")");
```

将输出的密文包裹在 `ENC(...)` 中填入配置文件。

### 6.3 需要加密的典型配置项

| 配置项 | 对应 YAML Key |
|--------|--------------|
| 数据库密码 | `spring.datasource.password` |
| Redis 密码 | `spring.data.redis.password` |
| 邮件服务密码 | `spring.mail.password` |
| 阿里云 OSS AccessKey | `mortise.file.oss.access-key-id` / `access-key-secret` |
| 微信相关密钥 | 各微信配置项 |

### 6.4 生产环境强制加密

> ⚠️ **强烈建议生产环境所有敏感配置均使用 `ENC(...)` 加密**，绝不将明文凭据提交至版本库或写入生产配置文件。

**生产环境操作流程**：
1. 确定好 `ENCRYPTION_KEY` 主密钥（建议 32 位以上随机字符串），通过服务器环境变量或密钥管理系统（如 Vault）注入，**不要写入任何配置文件**。
2. 使用 `JasyptUtils.encryptPassword("plain_value")` 生成各敏感配置项的密文。
3. 将 `ENC(密文)` 填入 `application-prod.yml`，明文从配置文件中彻底移除。
4. 通过 CI/CD 将 `ENCRYPTION_KEY` 注入为部署环境变量（如 Docker 的 `-e ENCRYPTION_KEY=xxx` 或 Kubernetes Secret）。

**开发环境说明**：本地开发时若使用的是独立的本地数据库且**不含生产凭据**，可在 `application-dev.yml` 中填明文以简化调试：

```yaml
spring:
  datasource:
    password: your_local_dev_password   # 仅本地开发数据库，绝不包含生产凭据
```

此时无需设置 `ENCRYPTION_KEY`，但**一旦配置中含有任何生产/测试环境凭据，必须使用加密**。

---

## 7. 编译与启动

### 7.1 编译

```bash
# 全量编译（跳过测试）
mvn clean package -DskipTests

# 仅验证编译某模块
mvn -pl mortise-app -am clean compile -DskipTests
```

### 7.2 启动 Spring Boot 应用

```bash
# 方式 A：Maven 插件（开发推荐，支持热重载）
cd mortise-app
mvn spring-boot:run

# 方式 B：jar 包
java -jar mortise-app/target/mortise-app-*.jar

# 方式 C：指定 profile
java -jar mortise-app/target/mortise-app-*.jar --spring.profiles.active=dev
```

> 需确保 `ENCRYPTION_KEY` 环境变量已在当前 Shell 中设置。

### 7.3 验证启动

```bash
# 健康检查（无需认证）
curl http://localhost:9999/mortise/actuator/health
```

返回 `{"status":"UP"}` 表示启动成功。

---

## 8. 商业模块日常维护

### 8.1 更新子模块到远端最新

```bash
git submodule update --remote --merge
```

### 8.2 提交子模块指针到主仓

```bash
git add .gitmodules mortise-product mortise-commerce mortise-payment
git commit -m "chore: update commercial submodule pointers"
git push origin master
```

> 主仓提交的是 gitlink（子模块引用），不是商业源码本体。

### 8.3 新增商业模块（维护流程）

以 `mortise-xxx` 为例：

```bash
# 1. 在 GitHub 创建私有仓库 rymcu/mortise-xxx，推送模块代码到 master

# 2. 主仓挂载子模块
git submodule add -b master git@github.com:rymcu/mortise-xxx.git mortise-xxx

# 3. 提交主仓引用
git add .gitmodules mortise-xxx
git commit -m "feat: add mortise-xxx as git submodule"
git push origin master
```

维护约定：
1. 在每个商业模块目录维护独立 README（接入、更新、排障）。
2. 同步更新本文件与主仓 README 的商业模块说明。
3. 禁止将商业模块源码直接合并进主仓。

---

## 9. 常见问题

### Q: `Permission denied (publickey)`

SSH 认证失败，检查：
1. GitHub 账号是否已添加当前机器公钥（`~/.ssh/id_ed25519.pub`）。
2. `~/.ssh/config` 是否正确配置了 `IdentityFile`。
3. 执行 `ssh -T git@github.com` 是否返回 `Hi <username>!`。

### Q: `repository not found`（子模块拉取时）

当前账号无该私有模块权限，联系仓库管理员开通后重试。

### Q: 启动报 `dataSource required` / 数据库密码为空 / `decryption` 异常

配置文件包含 `ENC(...)` 加密值，但 `ENCRYPTION_KEY` 环境变量未设置或值不正确，导致 Jasypt 无法解密，相关配置项变为空。

先确认当前 Shell 中变量是否已生效：
```bash
echo $ENCRYPTION_KEY          # Linux/macOS
echo $env:ENCRYPTION_KEY      # PowerShell
```

注意：`export` / `$env:` 只对**当前 Shell 会话**有效，通过 IDE 启动时需在 IDE 的运行配置中单独设置环境变量，或在系统级别持久化该变量。

### Q: 切换分支后子模块目录为空

```bash
git submodule update --init --recursive
```

### Q: 数据库连接失败

确认 Docker 依赖服务已就绪：
```bash
docker compose ps
docker compose logs postgresql
```

---

## 10. 推荐阅读

- [前端快速上手](FRONTEND_QUICK_START.md)
- [商业子模块开发上手手册](COMMERCIAL_MODULE_DEVELOPMENT.md)
- [商业子模块开发手册（后端版）](COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md)
- [商业子模块开发手册（前端 Layer 版）](COMMERCIAL_MODULE_FRONTEND_LAYER.md)
- 商业模块说明：各模块目录下的 `README.md`
- [架构说明](../architecture/architecture.md)
- [安全配置文档](../security/)
