# 🚀 Mortise 快速开始（当前项目版）

本指南用于当前 mortise 主仓的快速接入与运行，覆盖：

- 基础版（不含商业模块）
- 商业版（含私有 Submodule）
- 仅购买部分商业模块的按需拉取
- 开发人员新增商业模块的维护流程

---

## 1. 环境要求

- JDK 21+
- Maven 3.9+
- Docker / Docker Compose（可选，推荐用于快速体验）
- Git 2.30+

---

## 2. 克隆方式（按场景选择）

### 2.1 基础版（不拉取商业模块）

```bash
git clone https://github.com/rymcu/mortise.git
cd mortise
```

### 2.2 商业版（拉取私有商业模块）

> 需要已获得对应私有仓库权限，并正确配置 SSH Key。

```bash
git clone --recurse-submodules git@github.com:rymcu/mortise.git
cd mortise
```

### 2.3 已有仓库补拉子模块

```bash
git submodule update --init --recursive
```

---

## 3. 只购买了部分商业模块怎么办？

不要全量初始化，按需指定模块即可：

```bash
git clone git@github.com:rymcu/mortise.git
cd mortise

# 示例：仅拉取 payment + commerce
git submodule update --init --recursive mortise-payment mortise-commerce
```

如果已全量拉取但本地不需要某模块：

```bash
# 示例：停用 product
git submodule deinit -f mortise-product
```

---

## 4. 启动项目

### 4.1 方式一：Docker Compose（推荐）

```bash
docker compose up -d
```

查看状态：

```bash
docker compose ps
```

### 4.2 方式二：本地编译运行

```bash
mvn clean compile -DskipTests
```

如果只验证某模块（示例）：

```bash
mvn -pl mortise-system/mortise-system-admin -am clean compile -DskipTests
```

---

## 5. 商业模块日常维护

### 5.1 更新子模块到远端最新

```bash
git submodule update --remote --merge
```

### 5.2 主仓提交子模块指针

```bash
git add .gitmodules mortise-product mortise-commerce mortise-payment
git commit -m "chore: update commercial submodule pointers"
git push origin master
```

> 主仓提交的是子模块引用（gitlink），不是商业模块源码本体。

---

## 6. 开发人员新增商业模块（维护流程）

以 `mortise-xxx` 为例：

```bash
# 1) 先在 GitHub 创建私有仓库 rymcu/mortise-xxx，并推送模块代码到 master

# 2) 在主仓添加子模块
git submodule add -b master git@github.com:rymcu/mortise-xxx.git mortise-xxx

# 3) 提交主仓引用
git add .gitmodules mortise-xxx
git commit -m "feat: add mortise-xxx as git submodule"
git push origin master
```

维护建议：

1. 同步更新本文件与主仓 README 的商业模块说明。
2. 在每个商业模块目录维护独立 README（接入、更新、排障）。
3. 禁止将商业模块源码直接合并进主仓。

---

## 7. 常见问题

### 7.1 `Permission denied (publickey)`

说明 SSH 未认证成功，建议检查：

1. GitHub 账号是否已添加当前机器公钥。
2. `~/.ssh/config` 是否固定了正确 `IdentityFile`。
3. 执行 `ssh -T git@github.com` 是否能返回 `Hi <username>!`。

### 7.2 `repository not found`

通常是当前账号没有该私有模块权限，联系仓库管理员开通后重试。

### 7.3 切换分支后子模块目录为空

执行：

```bash
git submodule update --init --recursive
```

---

## 8. 推荐阅读

- 主仓说明：`README.md`
- 商业模块说明：`mortise-product/README.md`、`mortise-commerce/README.md`、`mortise-payment/README.md`
