# 发布剧本

以下命令以当前 88.146 主机和 `/opt/mortise` 目录为准。

## 标准脚本入口

优先使用仓库根目录脚本，避免重复手抄命令：

```powershell
# 仅发布后端
pwsh .\scripts\deploy-mortise-app.ps1

# 发布 standalone 社区到根路径，并同时发布后端
pwsh .\scripts\deploy-community-standalone.ps1 -SiteMode standalone-root

# 仅发布站点，不动后端
pwsh .\scripts\deploy-community-standalone.ps1 -SiteMode standalone-root -SkipBackend

# 发布官网社区模式（/community/*）
pwsh .\scripts\deploy-community-standalone.ps1 -SiteMode site-community -SkipBackend

# 单独执行 smoke
pwsh .\scripts\smoke-community.ps1 -SiteMode standalone-root
```

```bash
# 仅发布后端
./scripts/deploy-mortise-app.sh

# 发布 standalone 社区到根路径，并同时发布后端
./scripts/deploy-community-standalone.sh --site-mode standalone-root

# 仅重发前端，不动后端
./scripts/deploy-community-standalone.sh --site-mode standalone-root --skip-backend

# 发布官网社区模式（/community/*）
./scripts/deploy-community-standalone.sh --site-mode site-community --skip-backend

# 单独执行 smoke
./scripts/smoke-community.sh --site-mode standalone-root
```

脚本约定：

- `-Host` 默认 `192.168.88.146`
- `--host` 默认 `192.168.88.146`
- `deploy-community-standalone.ps1` 支持 `-SiteMode standalone-root|site-community`
- `deploy-community-standalone.sh` 支持 `--site-mode standalone-root|site-community`
- 管理端 dashboard 验收通过环境变量注入认证：
  - `MORTISE_ADMIN_BEARER_TOKEN`
  - `MORTISE_ADMIN_COOKIE`

## 后端：发布 `mortise-app`

适用场景：后端 jar 已在本地构建完成，准备替换线上运行包。

### 步骤

1. 上传新 jar 到 `/opt/mortise/mortise.jar`
2. 在远端重建镜像
3. 仅重启 `mortise-app`
4. 验证健康状态

### 常用命令

```powershell
scp D:\path\to\mortise.jar root@192.168.88.146:/opt/mortise/mortise.jar

ssh root@192.168.88.146 "docker build -f /opt/mortise/Dockerfile.runtime -t mortise-app:latest /opt/mortise && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-app && curl -s http://127.0.0.1:9999/mortise/actuator/health"
```

## 前端 Site：本地打包后部署

适用场景：修改了 `frontend/apps/site`、`frontend/layers/*`、共享包后，需要发布站点 SSR。

### 关键事实

- 远端通常只有 `site/.output`，不是完整前端源码
- 可以在本地直接 `pnpm build`，再把 `.output` 上传到远端
- 如果本地是 Windows 构建，`better-sqlite3` 可能需要在远端修成 Linux/Node 22 可用版本

### 本地构建

```powershell
Set-Location D:\workspace\mortise\frontend
pnpm --filter @mortise/site build
```

### 远端备份并上传 `.output`

```powershell
$backup = (ssh root@192.168.88.146 'cd /opt/mortise/frontend/site && backup=.output.bak-$(date +%Y%m%d%H%M%S) && mv .output $backup && echo $backup').Trim()

tar -C D:\workspace\mortise\frontend\apps\site -cf - .output | ssh root@192.168.88.146 'cd /opt/mortise/frontend/site && tar -xf -'
```

### 重建 `better-sqlite3`（Windows 打包产物常见必修步骤）

如果站点启动后出现 `ERR_DLOPEN_FAILED` 或 `Module did not self-register`，执行下面的远端修复：

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

### 重建镜像并重启站点

```powershell
ssh root@192.168.88.146 "docker build -f /opt/mortise/frontend/Dockerfile.site -t mortise-site:latest /opt/mortise/frontend && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-site"
```

### 验证

优先验证具体业务路由，不要只看 `/`：

```powershell
ssh root@192.168.88.146 "curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/community && docker logs --tail 40 mortise-site"
```

期望结果：

- `/community` 返回 `200`
- 日志只显示 `Listening on http://0.0.0.0:3000` 之类正常启动信息

### standalone-root 与 site-community 的验证口径

| `SiteMode` | 发布槽位 | 路由验证 |
|---|---|---|
| `standalone-root` | 仍复用 `/opt/mortise/frontend/site/.output` | `/topics`、`/collections` |
| `site-community` | `/opt/mortise/frontend/site/.output` | `/community`、`/community/collections` |

## 前端 Admin：本地打包后部署

Admin 与 Site 流程类似，但通常没有 `better-sqlite3` 这个坑。

### 本地构建

```powershell
Set-Location D:\workspace\mortise\frontend
pnpm --filter @mortise/admin build
```

### 上传并重启

```powershell
$backup = (ssh root@192.168.88.146 'cd /opt/mortise/frontend/admin && backup=.output.bak-$(date +%Y%m%d%H%M%S) && mv .output $backup && echo $backup').Trim()

tar -C D:\workspace\mortise\frontend\apps\admin -cf - .output | ssh root@192.168.88.146 'cd /opt/mortise/frontend/admin && tar -xf -'

ssh root@192.168.88.146 "docker build -f /opt/mortise/frontend/Dockerfile.admin -t mortise-admin:latest /opt/mortise/frontend && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-admin"
```

### 验证

```powershell
ssh root@192.168.88.146 "curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3001/admin/"
```

## 回滚与首轮观察

### 回滚

- 后端：恢复最近的 `/opt/mortise/mortise.jar.bak-*`，重建 `mortise-app`
- 站点：恢复最近的 `/opt/mortise/frontend/site/.output.bak-*`，重建 `mortise-site`
- 优先使用发布脚本输出的回滚命令，避免手工拼接路径

### 首轮观察项

- `http://127.0.0.1:9999/mortise/actuator/health` 返回 `UP`
- 站点业务路由返回 `200`
- `docker logs --tail 80 mortise-site` 中没有：
  - `ERR_DLOPEN_FAILED`
  - `better_sqlite3.node`
  - `Module did not self-register`
- 登录态下可额外检查：
  - `/api/v1/admin/community/dashboard/overview`
  - `/api/v1/admin/community/dashboard/trends`
  - `/api/v1/admin/community/dashboard/pending`
