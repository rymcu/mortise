# mortise-agent

## 模块介绍

`mortise-agent` 是 Mortise 的 AI Agent 业务域模块，负责 AI 模型接入、对话编排与智能体运行时管理，当前已覆盖以下核心链路：

- AI 供应商（Provider）与模型（Model）的增删改查与状态管理
- 多模型适配（OpenAI、Anthropic、Ollama）的统一对话接口
- 用户会话管理（创建、历史、删除）
- SSE 流式对话输出
- 意图分类与 Function Calling 支持

该模块采用 Mortise 标准业务域拆分方式，分为 `domain / kernel / application / infra / admin / api` 六层。相比标准五层模型，额外增加了 `kernel` 层，用于承载与 Spring AI 绑定的 SPI 抽象，使 Domain 与 Application 不直接依赖具体 AI 框架。

## 架构设计

### 模块分层

```text
mortise-agent/
├── mortise-agent-domain        领域对象、仓储接口、查询条件
├── mortise-agent-kernel        AI 运行时 SPI、模型抽象、配置
├── mortise-agent-application   CQRS 服务、对话编排、意图策略
├── mortise-agent-infra         Mapper、持久化实体、Provider 实现、仓储实现
├── mortise-agent-admin         管理端控制器（Provider/Model CRUD）
└── mortise-agent-api           用户端 API（对话、流式聊天）
```

### 层内职责

- `mortise-agent-domain`：定义 AI 供应商（`AiProvider`）、模型（`AiModel`）、会话（`Conversation`）等纯领域实体（`entity.*`），仓储端口接口（`repository.*`）和查询条件模型（`model.*`）；不依赖 `mortise-persistence`、MyBatis-Flex、Spring Web。
- `mortise-agent-kernel`：承载 AI 运行时的 SPI 扩展点，包括 `ChatModelProvider`（多模型统一对话接口）和 `IntentClassifier`（意图分类 SPI），以及模型类型枚举（`ModelType`）、消息/响应 DTO（`ChatMessage`、`ChatResponse`、`TokenUsage`）和配置属性（`AgentProperties`）；仅依赖 `spring-ai-core`，不依赖 Mortise 业务层。
- `mortise-agent-application`：承载 CQRS 风格的应用服务——Command 服务（`service.command.*`）处理 Provider/Model/Conversation 的写操作，Query 服务（`service.query.*`）处理读操作，`AgentService`（`service.chat.*`）编排对话流程；只依赖 Domain 和 Kernel，不直接依赖 Infra。
- `mortise-agent-infra`：承载 MyBatis-Flex Mapper（`mapper.*`）、持久化实体（`infra.persistence.entity.*`）、仓储实现（`infra.repository.*`）和 AI 供应商实现（`infra.provider.*`：OpenAI、Anthropic、Ollama），负责 Domain 与持久化对象之间的转换。
- `mortise-agent-admin`：提供管理端接口；Controller（`admin.controller.*`）通过 Facade（`admin.facade.*`）与 Assembler（`admin.assembler.*`）调用 Application，不直接依赖 Service/Entity/Mapper。
- `mortise-agent-api`：提供用户端对话 API；Controller（`api.controller.*`）通过 Facade（`api.facade.*`）调用 Application，支持常规请求和 SSE 流式输出。

### CQRS 分离

Application 层严格区分 Command（写）和 Query（读）服务：

- **Command 路径**：`application.service.command.ai.*`（Provider/Model 增删改）、`application.service.command.conversation.*`（会话管理）
- **Query 路径**：`application.service.query.ai.*`（Provider/Model 查询）、`application.service.query.conversation.*`（会话历史查询）
- Query 服务禁止依赖 Command 服务，Command 服务禁止依赖 Query 服务和 Result DTO，由 ArchUnit 测试强制执行。

### 架构守卫

- Maven Enforcer：Domain 层开启依赖守卫，禁止直接依赖 `mortise-persistence`、MyBatis-Flex、Spring Web、Jackson Databind；Admin/API 层开启依赖守卫，禁止直接依赖 Domain/Infra 模块；Application 层禁止依赖 Infra 模块。
- ArchUnit：共 **10 个**架构测试（位于 `mortise-agent-infra` 测试目录），全部使用包级规则：
  - 分层契约：AdminApi → Application → Domain/Kernel，Infrastructure → Domain/Kernel，不允许逆向依赖。
  - Controller 约束：只允许依赖 Contract DTO 和 Facade，不允许直接触达 Application/Kernel/Entity/Mapper。
  - Facade 约束：只允许协调 Contract/Assembler/Application，不允许依赖 Controller/Entity/Mapper/Kernel。
  - Assembler 约束：纯转换层，不允许依赖 Controller/Facade/Entity/Mapper/Kernel。
  - Application → Infra 隔离：Application 包禁止依赖 Infra/Mapper 包。
  - CQRS 双向隔离：Query 服务禁止依赖 Command 服务，反之亦然。
  - Domain 与 Kernel 隔离：Domain 不依赖 Kernel/Application/Infra/Admin/API；Kernel 不依赖 Domain/Application/Infra/Admin/API。
  - Legacy 服务隔离：Facade 禁止依赖已标记为遗留的混合服务包。

### Kernel SPI 扩展点

- `ChatModelProvider`：统一多模型对话接口，方法包括 `chat()`、`chatWithFunctions()`、`isAvailable()`、`getModelType()`。Infra 层提供 OpenAI/Anthropic/Ollama 三个实现。
- `IntentClassifier`：意图分类 SPI，输入用户消息和可用工具列表，输出 `IntentResult`。
- `ChatModelProviderRegistry`：Provider 发现与注册中心，运行时按 `ModelType` 查找可用 Provider。

### 与其他模块的关系

- `mortise-auth`：用户端 Agent API 依赖 `mortise-auth` 的认证链，对话接口要求 `isAuthenticated()`。
- `mortise-web-support`：Admin 和 API 控制器依赖 `mortise-web-support` 的 `@AdminController`、`@ApiController` 元注解和 `GlobalResult` 返回包装。
- `mortise-core`：Domain 和 Application 依赖 `mortise-core` 的通用分页模型（`PageQuery`、`PageResult`）和基础工具。

## 已落地接口

### 管理端

- `GET /api/v1/admin/agent/providers`：AI 供应商分页查询
- `GET /api/v1/admin/agent/providers/{id}`：供应商详情
- `POST /api/v1/admin/agent/providers`：新增供应商
- `PUT /api/v1/admin/agent/providers/{id}`：更新供应商
- `DELETE /api/v1/admin/agent/providers/{id}`：删除供应商
- `PUT /api/v1/admin/agent/providers/{id}/enable`：启用供应商
- `PUT /api/v1/admin/agent/providers/{id}/disable`：停用供应商
- `PATCH /api/v1/admin/agent/providers/{id}/status`：更新供应商状态
- `GET /api/v1/admin/agent/models`：AI 模型分页查询
- `GET /api/v1/admin/agent/models/{id}`：模型详情
- `POST /api/v1/admin/agent/models`：新增模型
- `PUT /api/v1/admin/agent/models/{id}`：更新模型
- `DELETE /api/v1/admin/agent/models/{id}`：删除模型
- `PUT /api/v1/admin/agent/models/{id}/enable`：启用模型
- `PUT /api/v1/admin/agent/models/{id}/disable`：停用模型
- `PATCH /api/v1/admin/agent/models/{id}/status`：更新模型状态

### 用户端

- `GET /api/v1/agent/models`：获取可用模型列表
- `GET /api/v1/agent/conversations`：获取当前用户会话列表
- `DELETE /api/v1/agent/conversations/{id}`：删除会话
- `POST /api/v1/agent/chat`：发送消息并获取回复
- `GET /api/v1/agent/chat/stream`：SSE 流式对话
