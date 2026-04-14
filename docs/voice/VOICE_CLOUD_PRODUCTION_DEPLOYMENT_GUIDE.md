# 语音云上最小生产部署指南

本文档将 `mortise-voice` 当前能力落到“中国内地用户 / 小规模 / 已备案 / 云 TTS”的最小生产形态。

目标形态：

- 标准云服务器，`不使用轻量应用服务器`
- 中国内地同地域部署，默认优先华东（上海）
- `mortise-app` 对外提供统一 HTTPS API
- `voice-runtime` 仅在同 VPC 内网提供 ASR runtime
- `TTS` 第一阶段优先走云厂商 Provider
- TTS 音频 artifact 落到 S3 兼容对象存储

当前仓库可直接复用的样例文件：

- [../../config/systemd/mortise-app.service](../../config/systemd/mortise-app.service)
- [../../config/nginx/mortise-api.voice-public.conf.example](../../config/nginx/mortise-api.voice-public.conf.example)
- [../../config/prometheus/mortise-voice-alert-rules.example.yml](../../config/prometheus/mortise-voice-alert-rules.example.yml)
- [../../config/voice-runtime/.env.example](../../config/voice-runtime/.env.example)
- [../../config/systemd/mortise-voice-runtime.service](../../config/systemd/mortise-voice-runtime.service)
- [../../config/nginx/voice-runtime.internal.conf](../../config/nginx/voice-runtime.internal.conf)
- [./VOICE_RUNTIME_PRODUCTION_DEPLOYMENT_GUIDE.md](./VOICE_RUNTIME_PRODUCTION_DEPLOYMENT_GUIDE.md)

## 1. 推荐拓扑

```text
Internet
   |
   v
App VM (ECS/CVM, 2c4g, 5 Mbps)
  ├─ Nginx 443
  ├─ mortise-app :9999
  └─ /etc/mortise/mortise.env
   |
   |  内网 HTTP，同地域同 VPC
   v
Voice Runtime VM (ECS/CVM, 4c8g)
  ├─ Nginx :80
  ├─ Docker Compose
  └─ sherpa-onnx + FastAPI :127.0.0.1:17778

对象存储 Bucket
  └─ TTS 音频 artifact / 可选 ASR 原音频归档
```

推荐原因：

- 公网只暴露 `mortise-app`，runtime 推理节点不直接暴露给用户
- ASR 与主应用拆开，避免语音推理抢占业务 CPU
- TTS 先接云 Provider，控制首发成本和复杂度
- 后续扩容时，可独立升级 `voice-runtime` 到 `8 vCPU / 16 GB`

## 2. 采购清单

| 资源 | 建议规格 | 用途 |
| --- | --- | --- |
| App VM | 2 vCPU / 4 GB RAM / 80 GB ESSD / 5 Mbps 公网 | 承载 `mortise-app` 与公网 Nginx |
| Voice Runtime VM | 4 vCPU / 8 GB RAM / 100 GB ESSD / 无公网 IP | 承载 `SenseVoice int8` ASR runtime |
| 对象存储 Bucket | 标准存储 + 生命周期策略 | 存放 TTS 音频 artifact |
| TLS 证书 | 单域名或泛域名 | 只给 `mortise-app` 公网入口 |
| 监控告警 | Prometheus / Grafana / 告警渠道 | CPU、内存、磁盘、HTTP 5xx、ASR/TTS 超时 |

说明：

- 如果主应用已在中国内地某地域运行，优先沿用现有地域，并把 `voice-runtime` 放到同地域、同 VPC。
- 如果数据库、Redis 已托管或已有生产实例，本次不重复采购。
- `voice-runtime` 不分配公网 IP，运维入口通过堡垒机、办公网或云厂商远程连接能力进入。

## 3. 网络与安全边界

公网只暴露以下用户接口：

- `POST /api/v1/voice/asr/recognize-once`
- `POST /api/v1/voice/tts/synthesize`
- `GET /api/v1/voice/jobs`
- `GET /api/v1/voice/jobs/{id}`

`voice-runtime` 仅暴露以下内网接口：

- `GET /health`
- `POST /asr/recognize-once`
- 预留 `POST /tts/synthesize`

推荐策略：

- App VM 的安全组仅开放 `80/443`；`22` 只对白名单运维网段开放
- Voice Runtime VM 的安全组仅开放来自 App VM 或办公网段的 `80`
- Docker 内部端口 `17778` 只绑定 `127.0.0.1`
- 使用 [../../config/nginx/mortise-api.voice-public.conf.example](../../config/nginx/mortise-api.voice-public.conf.example) 给语音接口加 `HTTPS`、限流和上传大小限制
- 使用 [../../config/nginx/voice-runtime.internal.conf](../../config/nginx/voice-runtime.internal.conf) 约束 runtime 只接受内网来源

## 4. App VM 部署

### 4.1 准备目录

```bash
sudo useradd --system --home /opt/mortise --shell /usr/sbin/nologin mortise || true
sudo mkdir -p /opt/mortise /etc/mortise
sudo chown -R mortise:mortise /opt/mortise /etc/mortise
```

### 4.2 部署应用

将 `mortise-app` 打包产物上传到 `/opt/mortise/mortise-app.jar`。

复制 systemd 样例：

- [../../config/systemd/mortise-app.service](../../config/systemd/mortise-app.service) -> `/etc/systemd/system/mortise-app.service`

复制环境变量：

- 根目录 [../../.env.example](../../.env.example) 中与 Spring Boot 生产环境相关的变量 -> `/etc/mortise/mortise.env`

最少要确认以下变量：

```env
SPRING_PROFILES_ACTIVE=prod
BASE_URL=https://api.example.com

POSTGRES_HOST=postgres.internal
POSTGRES_PORT=5432
POSTGRES_DB=postgres
POSTGRES_SCHEMA=mortise
POSTGRES_USER=mortise
POSTGRES_PASSWORD=changeme

REDIS_HOST=redis.internal
REDIS_PORT=6379
REDIS_PASSWORD=changeme

VOICE_RUNTIME_NODE_ID=voice-runtime-prod-01
VOICE_RUNTIME_BASE_URL=http://voice-runtime.internal
VOICE_RUNTIME_ENABLED=true
VOICE_RUNTIME_CONNECT_TIMEOUT_MILLIS=1500
VOICE_RUNTIME_READ_TIMEOUT_MILLIS=30000
VOICE_RUNTIME_PREWARM_MODEL_1=sense-voice-zh-en-ja-ko-yue-int8

FILE_STORAGE_DEFAULT_PLATFORM=amazon-s3-1
RUSTFS_ACCESS_KEY=changeme
RUSTFS_SECRET_KEY=changeme
RUSTFS_ENDPOINT=https://storage.example.com
RUSTFS_BUCKET=mortise-prod
RUSTFS_DOMAIN=https://static.example.com/
RUSTFS_BASE_PATH=voice/
```

说明：

- `RUSTFS_*` 变量名是历史兼容名，但生产环境可以直接指向 S3 兼容对象存储
- `VOICE_RUNTIME_BASE_URL` 应配置为内网 DNS 或固定内网 IP，不要指向公网地址

### 4.3 启动应用

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now mortise-app
sudo systemctl status mortise-app
```

### 4.4 部署公网 Nginx

复制 Nginx 样例：

- [../../config/nginx/mortise-api.voice-public.conf.example](../../config/nginx/mortise-api.voice-public.conf.example) -> `/etc/nginx/conf.d/mortise-api.voice-public.conf`

上线前至少替换：

- `api.example.com`
- `ssl_certificate` / `ssl_certificate_key`
- `/mortise/actuator/health` 的白名单网段

该样例已经处理：

- `HTTPS` 入口
- `ASR/TTS` 接口的限流
- `20 MB` 上传大小限制
- 语音接口更长的代理超时

## 5. Voice Runtime VM 部署

Voice Runtime VM 继续沿用独立运行时手册：

- [./VOICE_RUNTIME_PRODUCTION_DEPLOYMENT_GUIDE.md](./VOICE_RUNTIME_PRODUCTION_DEPLOYMENT_GUIDE.md)

部署时补充以下约束：

- 运行时 VM 与 App VM 位于同地域、同 VPC
- `VOICE_RUNTIME_BIND_HOST=127.0.0.1`
- Nginx 只对白名单内网 IP 放行
- `server_name` 建议使用内网 DNS，例如 `voice-runtime.internal`

## 6. 对象存储约定

第一阶段建议：

- `ASR transcript` 继续落数据库 / artifact 记录
- `TTS audio` 优先落对象存储，再把公开或签名 URL 回填到 artifact
- ASR 原始音频可按需归档到同一 Bucket 的单独前缀，例如 `voice/asr-source/`

Bucket 规则建议：

- 开启版本控制或至少保留最近版本
- 配置生命周期策略，对历史 TTS 音频和源文件做低频或归档迁移
- 如无公网直读需求，默认走私有 Bucket + 签名 URL

## 7. 监控与告警

建议最少接入以下抓取目标：

- `mortise-app` 的 `/mortise/actuator/prometheus`
- `voice-runtime` 的 `/health` 黑盒探测
- App VM / Runtime VM 的 node exporter

可直接复用的示例告警规则：

- [../../config/prometheus/mortise-voice-alert-rules.example.yml](../../config/prometheus/mortise-voice-alert-rules.example.yml)

默认覆盖：

- `mortise-app` 不可用
- voice API 5xx 升高
- `ASR / TTS` P95 超时
- `voice-runtime` 健康探测失败
- 语音相关节点 CPU / 内存 / 磁盘压力

## 8. 验证清单

### 8.1 冒烟

公网调用以下接口各 10 次，确认全部 `2xx`：

- `POST /api/v1/voice/asr/recognize-once`
- `POST /api/v1/voice/tts/synthesize`

同时确认：

- `VoiceJob` 成功入库
- artifact 可回查
- TTS 音频 URL 可访问或可签名下载

### 8.2 性能基线

按以下基线验证：

- `ASR` 并发 `3`
- `TTS` `10` 次 / 分钟

满足以下任一条件时，直接把 Runtime VM 升到 `8 vCPU / 16 GB`：

- ASR 节点 CPU 持续高于 `70%`
- `POST /api/v1/voice/asr/recognize-once` 的 `P95` 超过 `6s`

### 8.3 稳定性

检查以下项：

- `prewarmModels` 是否生效
- App VM -> Runtime VM 内网探测稳定
- `voice-runtime` 日志按预期落盘
- TTS 音频上传对象存储成功
- `VoiceJob` / artifact 查询可回放完整链路

## 9. 首发默认值

第一阶段默认采用以下策略：

- `ASR` 自建 runtime，`TTS` 走云 Provider
- 不上双活，不上专用 GPU
- 主应用与 runtime 拆机，不与数据库、Redis 混部
- 只在运行时压力或 `P95` 超阈值时再升级实例规格
