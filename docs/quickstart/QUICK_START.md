# 🚀 Mortise 快速开始

本指南覆盖从环境准备到应用运行的完整流程，包括：

- 本地开发环境搭建
- Docker Compose 一键启动依赖
- 敏感配置加密（Jasypt）
- 商业模块的按需拉取与维护

---

## 1. 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 21+ | 推荐 Eclipse Temurin 21 |
| **Maven** | 3.9+ | `mvn -version` 验证 |
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

## 3. 启动依赖服务

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

## 4. 配置应用

### 4.1 配置文件说明

| 文件 | 用途 |
|------|------|
| `mortise-app/src/main/resources/application.yml` | 指定激活的 profile（Maven 注入） |
| `mortise-app/src/main/resources/application-dev.yml` | 开发环境配置（数据库、Redis、邮件等） |
| `mortise-app/src/main/resources/application-prod.yml` | 生产环境配置 |

### 4.2 设置加密密钥环境变量

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

## 5. Jasypt 敏感配置加密

### 5.1 原理

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

### 5.2 生成密文

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

### 5.3 需要加密的典型配置项

| 配置项 | 对应 YAML Key |
|--------|--------------|
| 数据库密码 | `spring.datasource.password` |
| Redis 密码 | `spring.data.redis.password` |
| 邮件服务密码 | `spring.mail.password` |
| 阿里云 OSS AccessKey | `mortise.file.oss.access-key-id` / `access-key-secret` |
| 微信相关密钥 | 各微信配置项 |

### 5.4 生产环境强制加密

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

## 6. 编译与启动

### 6.1 编译

```bash
# 全量编译（跳过测试）
mvn clean package -DskipTests

# 仅验证编译某模块
mvn -pl mortise-app -am clean compile -DskipTests
```

### 6.2 启动 Spring Boot 应用

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

### 6.3 验证启动

```bash
# 健康检查（无需认证）
curl http://localhost:9999/mortise/actuator/health
```

返回 `{"status":"UP"}` 表示启动成功。

---

## 7. 商业模块日常维护

### 7.1 更新子模块到远端最新

```bash
git submodule update --remote --merge
```

### 7.2 提交子模块指针到主仓

```bash
git add .gitmodules mortise-product mortise-commerce mortise-payment
git commit -m "chore: update commercial submodule pointers"
git push origin master
```

> 主仓提交的是 gitlink（子模块引用），不是商业源码本体。

### 7.3 新增商业模块（维护流程）

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

## 8. 常见问题

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

## 9. 推荐阅读

- [前端快速上手](FRONTEND_QUICK_START.md)
- 商业模块说明：各模块目录下的 `README.md`
- [架构说明](../architecture/architecture.md)
- [安全配置文档](../security/)
