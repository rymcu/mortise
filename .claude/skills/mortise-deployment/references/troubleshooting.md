# 部署排障

## 1. 远端找不到源码路径

### 现象

- 试图覆盖 `/opt/mortise/frontend/layers/community/...` 失败
- 远端只看到 `frontend/site/.output`、`frontend/admin/.output`

### 原因

当前线上目录是**运行产物目录**，不是完整 frontend monorepo 工作区。

### 处理

- 不要在远端按本地源码路径找 `.vue` 文件
- 改为本地构建 `.output` 后上传

## 2. Site 启动后 `better-sqlite3` 报错

### 典型报错

- `ERR_DLOPEN_FAILED`
- `Module did not self-register: ... better_sqlite3.node`

### 原因

本地（尤其 Windows）构建的 `.output` 带了不兼容的原生模块，但线上 `mortise-site` 运行在 Linux + Node 22。

### 处理

不要只替换旧的 `.node` 文件，优先在远端用 `node:22-slim` 容器**重新安装整包**：

```powershell
$script = @'
set -e

docker run --rm -v /opt/mortise/frontend/site/.output/server:/work node:22-slim bash -lc '
  apt-get update >/dev/null
  apt-get install -y python3 make g++ >/dev/null
  mkdir -p /tmp/sqlitefix
  cd /tmp/sqlitefix
  npm init -y >/dev/null
  npm install better-sqlite3@12.6.2 >/dev/null
  rm -rf /work/node_modules/better-sqlite3
  cp -a /tmp/sqlitefix/node_modules/better-sqlite3 /work/node_modules/
'
'@ -replace "`r", ""

$script | ssh root@192.168.88.146 "bash -s"
```

之后必须重新构建 `mortise-site` 镜像并重启容器。

## 3. `/` 不是好的站点验证地址

### 现象

- `curl -I http://127.0.0.1:3000/` 返回 404
- 但站点实际并未完全失败

### 原因

根路由不一定是这次变更涉及的有效验证路径，或者 HEAD/SSR 行为不适合作为唯一探针。

### 处理

优先验证具体业务路由：

- standalone 社区根路径：`http://127.0.0.1:3000/topics`、`http://127.0.0.1:3000/collections`
- 官网社区模式：`http://127.0.0.1:3000/community`、`http://127.0.0.1:3000/community/collections`
- 管理端：`http://127.0.0.1:3001/admin/`
- 后端：`http://127.0.0.1:9999/mortise/actuator/health`

## 4. 本地没有 Docker CLI

### 现象

- 本地 `docker` 命令不存在

### 处理

- Site/Admin 仍可先在本地使用 `pnpm --filter ... build`
- 再把 `.output` 上传到远端
- 镜像构建留在远端 Docker 主机执行

## 5. 回滚

如果新包启动失败，优先使用 `.output.bak-*` 或旧 jar 回滚：

1. 恢复备份产物
2. 重建对应镜像
3. `docker compose ... up -d <service>`
4. 再次执行路由或健康检查验证

## 6. PowerShell 下的时间戳与远端变量展开

### 现象

- 在 PowerShell 中直接执行 `ssh root@host "ts=$(date +%Y%m%d%H%M%S)"` 时，本地先把 `$(...)` 展开
- 可能出现 `Get-Date` 参数错误或生成异常备份路径

### 处理

- 优先在本地用 `Get-Date -Format yyyyMMddHHmmss` 生成时间戳，再拼入远端命令
- 如果必须让远端 bash 执行 `$(date ...)`，要用**单引号**包裹远端脚本，避免 PowerShell 预先展开
- 已固化到 `scripts/deploy-mortise-app.ps1` 与 `scripts/deploy-community-standalone.ps1`，优先复用脚本
