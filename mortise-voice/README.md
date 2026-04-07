# mortise-voice

## 模块介绍

`mortise-voice` 是 Mortise 的统一语音能力域模块，面向平台内部业务提供语音模型目录、语音配置目录、运行时状态查询与后续 ASR/TTS/VAD 编排能力。

当前实现优先完成以下基础设施：

- 六层模块拆分：`domain / kernel / application / infra / admin / api`
- 语音 Provider / Model / Profile 目录模型
- 运行时状态抽象与 sidecar client 契约
- 运行时节点列表同时返回静态配置状态与一次即时健康探测结果，二者语义分离
- 管理端目录 CRUD 接口、运行时查询接口与用户端 Profile 查询接口
- 用户端短音频同步 ASR 接口（multipart 上传 -> `VoiceJob` 入库 -> runtime client 同步识别 -> transcript artifact 落库）
- 用户端同步 TTS 接口（JSON 请求 -> `VoiceJob` 入库 -> runtime client 同步合成 -> 音频 artifact 落库或远端地址回填）
- 用户端 `VoiceJob` 列表/详情接口，可查询当前用户自己的任务与 artifact 信息
- 管理端 VoiceJob 分页查询与详情查询接口，以及对应任务记录页面与 artifact 链接展示
- Flyway 初始化表结构与 ArchUnit 分层守卫

当前用户端已提供以下核心接口：

- `POST /api/v1/voice/asr/recognize-once`
- `POST /api/v1/voice/tts/synthesize`
- `GET /api/v1/voice/jobs`
- `GET /api/v1/voice/jobs/{id}`

后续将在此基础上补齐：

- Job 重试、artifact 补偿与后续异步编排
- WebSocket 实时流式识别
- 云 TTS 供应商接入
- Job / Artifact / Session 状态机与审计