# 88.146 运行拓扑

## 目标主机

- 主机：`192.168.88.146`
- 部署根目录：`/opt/mortise`
- 当前服务编排文件：`/opt/mortise/docker-compose.app.yaml`

## Docker 服务

当前线上关键服务：

| Service | 作用 | 运行方式 |
|---|---|---|
| `mortise-app` | Spring Boot 后端 | Docker，host network |
| `mortise-site` | Nuxt Site SSR | Docker，host network |
| `mortise-admin` | Nuxt Admin | Docker，host network |

## 端口与代理

主机内部监听：

| 端口 | 服务 |
|---|---|
| `9999` | `mortise-app` |
| `3000` | `mortise-site` |
| `3001` | `mortise-admin` |

Nginx 负责 80 端口入口转发：

| 路径 | 上游 |
|---|---|
| `/mortise` | `127.0.0.1:9999` |
| `/admin` | `127.0.0.1:3001` |
| `/` | `127.0.0.1:3000` |
| `/health` | `127.0.0.1:9999/mortise/actuator/health` |

## 运行产物目录

### 后端

- Jar：`/opt/mortise/mortise.jar`
- 运行镜像 Dockerfile：`/opt/mortise/Dockerfile.runtime`

### 前端

远端 `frontend/` 当前主要保存**运行产物**与 Dockerfile，而不是完整源码树：

| 路径 | 含义 |
|---|---|
| `/opt/mortise/frontend/site/.output` | Site SSR 运行产物 |
| `/opt/mortise/frontend/admin/.output` | Admin 运行产物 |
| `/opt/mortise/frontend/Dockerfile.site` | Site 运行镜像 |
| `/opt/mortise/frontend/Dockerfile.admin` | Admin 运行镜像 |

> 不要默认远端存在 `frontend/layers/community/...` 这类源码路径。部署前先确认远端目录实际结构。

## 当前运行时特征

- `mortise-site` 当前镜像基于 `node:22-slim`
- `mortise-site` 内部监听 `3000`
- `mortise-admin` 当前运行时 Node 版本为 20.x，内部监听 `3001`
- `mortise-site` 通过 `NUXT_PUBLIC_API_BASE=http://192.168.88.146/mortise` 访问后端
- `mortise-admin` 通过 `NUXT_PUBLIC_API_BASE=/mortise` 访问后端
