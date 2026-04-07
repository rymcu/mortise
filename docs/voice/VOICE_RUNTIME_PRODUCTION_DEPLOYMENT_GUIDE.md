# 语音运行时生产部署指南

本文档提供 `mortise-voice` 外部语音运行时的生产部署方案，目标形态为：

- PVE 独立 KVM 虚拟机
- Ubuntu Server 24.04 LTS
- Docker Compose 承载 `sherpa-onnx + FastAPI` ASR sidecar
- Nginx 仅对内网暴露统一入口
- systemd 负责开机自启与统一启停
- 防火墙只放行管理与应用访问

> 仓库治理说明
>
> - 截至当前阶段，voice 能力仍处于接入主仓前的治理过渡期。
> - 本文中出现的 `mortise-voice`、类名或配置名，表示当前实现草稿或目标契约，不表示该模块已经作为主仓开源模块或商业子模块正式纳管。
> - voice 的开源/商业归属应按能力边界判断，不应按当前工作区是否存在目录、`pom.xml` 条目或临时文件结构判断。

当前仓库已提供以下生产样例文件：

- [../../config/voice-runtime/compose.yaml](../../config/voice-runtime/compose.yaml)
- [../../config/voice-runtime/Dockerfile](../../config/voice-runtime/Dockerfile)
- [../../config/voice-runtime/app.py](../../config/voice-runtime/app.py)
- [../../config/voice-runtime/requirements.txt](../../config/voice-runtime/requirements.txt)
- [../../config/voice-runtime/.env.example](../../config/voice-runtime/.env.example)
- [../../config/systemd/mortise-voice-runtime.service](../../config/systemd/mortise-voice-runtime.service)
- [../../config/nginx/voice-runtime.internal.conf](../../config/nginx/voice-runtime.internal.conf)

## 1. 适用范围

本方案适用于当前 `mortise-voice` 的最小生产运行链路：

- `GET /health`
- `POST /asr/recognize-once`

最小接口契约建议直接按本文约定执行：

- `GET /health`：返回 `2xx`，响应体建议包含 `status` 字段，值为 `UP` 或等价健康状态
- `POST /asr/recognize-once`：接收 `multipart/form-data`，至少包含 `profileCode` 与音频文件字段 `file`
- 识别成功后返回 `2xx`，响应体至少能提供识别文本；如有需要，可额外返回语言、时长、tokens、timestamps 等信息

如果后续要接入 TTS，请在同一 sidecar 中补齐 `/tts/synthesize`，或再拆一台独立 TTS runtime。

## 2. 推荐架构

建议拓扑如下：

```text
Mortise 应用服务器
        |
        |  内网 HTTP
        v
voice-runtime VM (PVE)
  ├─ Nginx :80
  ├─ Docker Compose
  └─ FastAPI + sherpa-onnx :127.0.0.1:17778
```

推荐原因：

- 语音推理与主应用隔离，避免 CPU 峰值互相影响
- 运行时升级、重启、替换模型不影响数据库与主业务服务
- 后续可单独横向扩容或做 GPU Passthrough

## 3. VM 规格建议

仅跑 `SenseVoice int8` 时建议：

- 4 vCPU
- 8 GB RAM
- 60 GB 系统盘
- 50 GB 数据盘或单独模型目录
- 固定内网 IP

后续若增加并发或叠加 TTS，可升到：

- 8 vCPU
- 16 GB RAM

## 4. 主机初始化

在 VM 内安装基础组件：

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin nginx ufw curl
sudo systemctl enable --now docker
sudo systemctl enable --now nginx
```

准备部署目录：

```bash
sudo mkdir -p /opt/mortise-voice-runtime/{models,logs}
sudo chown -R $USER:$USER /opt/mortise-voice-runtime
```

将以下文件复制到部署目录：

- [../../config/voice-runtime/compose.yaml](../../config/voice-runtime/compose.yaml) -> `/opt/mortise-voice-runtime/compose.yaml`
- [../../config/voice-runtime/Dockerfile](../../config/voice-runtime/Dockerfile) -> `/opt/mortise-voice-runtime/Dockerfile`
- [../../config/voice-runtime/app.py](../../config/voice-runtime/app.py) -> `/opt/mortise-voice-runtime/app.py`
- [../../config/voice-runtime/requirements.txt](../../config/voice-runtime/requirements.txt) -> `/opt/mortise-voice-runtime/requirements.txt`
- [../../config/voice-runtime/.env.example](../../config/voice-runtime/.env.example) -> `/opt/mortise-voice-runtime/.env`

复制 systemd 与 Nginx 配置：

- [../../config/systemd/mortise-voice-runtime.service](../../config/systemd/mortise-voice-runtime.service) -> `/etc/systemd/system/mortise-voice-runtime.service`
- [../../config/nginx/voice-runtime.internal.conf](../../config/nginx/voice-runtime.internal.conf) -> `/etc/nginx/conf.d/voice-runtime.internal.conf`

## 5. 下载模型

示例以 `SenseVoice zh/en/ja/ko/yue int8` 为例：

```bash
cd /opt/mortise-voice-runtime/models
curl -L -O https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09.tar.bz2
tar xf sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09.tar.bz2
rm sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09.tar.bz2
```

模型解压完成后，确认 `.env` 中以下路径与实际目录一致：

```env
VOICE_MODEL_DIR=/opt/mortise-voice-runtime/models
SENSEVOICE_MODEL=/models/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09/model.int8.onnx
SENSEVOICE_TOKENS=/models/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09/tokens.txt
```

## 6. 调整环境变量

编辑 `/opt/mortise-voice-runtime/.env`，至少确认以下字段：

```env
VOICE_RUNTIME_BIND_HOST=127.0.0.1
VOICE_RUNTIME_HOST_PORT=17778
VOICE_MODEL_DIR=/opt/mortise-voice-runtime/models
VOICE_RUNTIME_LOG_DIR=/opt/mortise-voice-runtime/logs
SENSEVOICE_MODEL_ALIAS=sense-voice-zh-en-ja-ko-yue-int8
```

这里采用 `127.0.0.1:17778` 绑定，是为了让 Docker 容器只向本机回环地址暴露端口，再由 Nginx 对内网做代理。这样能避免应用容器绕过 Nginx 直接被局域网访问。

## 7. 启动与自启

先做一次手工启动验证：

```bash
cd /opt/mortise-voice-runtime
docker compose up -d --build
docker compose ps
curl http://127.0.0.1:17778/health
```

确认无误后启用 systemd：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now mortise-voice-runtime
sudo systemctl status mortise-voice-runtime
```

常用运维命令：

```bash
sudo systemctl restart mortise-voice-runtime
sudo systemctl reload mortise-voice-runtime
docker compose -f /opt/mortise-voice-runtime/compose.yaml logs -f
```

## 8. Nginx 内网反代

[../../config/nginx/voice-runtime.internal.conf](../../config/nginx/voice-runtime.internal.conf) 做了两件事：

- 统一从 `:80` 暴露运行时接口
- 只允许指定的 Mortise 应用服务器 IP 访问

上线前请至少替换两处：

- `server_name voice-runtime.internal` 改成你的内网域名或保留为内网 DNS 记录
- `allow 10.0.0.20` 改成 Mortise 应用服务器的实际 IP

校验并重载 Nginx：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

如果你有内部 CA，也可以把该配置改为 `listen 443 ssl;`，再把 Mortise 的 `baseUrl` 改成 `https://...`。当前样例默认先走内网 HTTP，降低证书运维成本。

## 9. 防火墙规则

Ubuntu 环境建议直接使用 UFW。以下规则假设：

- `10.0.0.10` 是运维跳板机
- `10.0.0.20` 是 Mortise 应用服务器

执行前请替换成你的实际 IP：

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing

sudo ufw limit from 10.0.0.10 to any port 22 proto tcp comment 'ops-ssh'
sudo ufw allow from 10.0.0.20 to any port 80 proto tcp comment 'mortise-app'

sudo ufw enable
sudo ufw status verbose
```

说明：

- 不需要放开 `17778`，因为 Docker 端口只绑定到 `127.0.0.1`
- 如果启用了 HTTPS，则将 `80` 调整为 `443`
- 如果需要从 Prometheus 或运维探针访问 `/health`，再单独加白该探针 IP

## 10. 接入 Mortise

在 `mortise-app` 环境配置中加入 runtime 节点，例如：

```yaml
mortise:
  voice:
    runtime:
      nodes:
        - nodeId: sensevoice-pve-01
          baseUrl: http://voice-runtime.internal
          enabled: true
          prewarmModels:
            - sense-voice-zh-en-ja-ko-yue-int8
    asr:
      recognizePath: /asr/recognize-once
```

当前接入至少需要以下字段语义保持一致：

- `mortise.voice.runtime.connect-timeout-millis`
- `mortise.voice.runtime.read-timeout-millis`
- `mortise.voice.runtime.nodes[].nodeId`
- `mortise.voice.runtime.nodes[].baseUrl`
- `mortise.voice.runtime.nodes[].enabled`
- `mortise.voice.runtime.nodes[].prewarmModels`
- `mortise.voice.asr.recognizePath`

如果 voice 主模块尚未正式纳入当前仓库，请以本文档中的接口与字段约定为准，而不是依赖某个暂存源码目录是否存在。

后台目录维护建议：

- Provider：`LOCAL_RUNTIME`
- Model：能力选 `ASR`，类型选 `SENSEVOICE`
- `runtimeName`：`sense-voice-zh-en-ja-ko-yue-int8`
- Profile：绑定上述 Provider/Model

## 11. 验证清单

先验证 VM 本机回环接口：

```bash
curl http://127.0.0.1:17778/health
```

再验证经过 Nginx 的内网入口：

```bash
curl http://voice-runtime.internal/health
```

再验证 ASR：

```bash
curl -F "profileCode=sense-voice-default" \
  -F "file=@./test.wav;type=audio/wav" \
  http://voice-runtime.internal/asr/recognize-once
```

最后在 Mortise 管理端检查：

- 运行时节点为“已配置”
- 探测状态为“健康”
- 用户端 ASR 接口可返回识别文本

## 12. 运维建议

- 单台 VM 只承载语音 runtime，不与数据库或 Redis 混部
- 当前样例使用单 worker 和进程内锁，优先保证模型稳定性而不是极限并发
- 如果需要提高吞吐量，优先新增 VM 节点，再考虑单机多实例
- 模型升级时先新建目录，灰度更新 `.env`，验证通过后再切换 systemd 重载
- 如果未来要上 GPU，继续沿用 VM 方案，在 PVE 做 PCI Passthrough 即可
