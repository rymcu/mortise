# Project-Level Copilot Instructions

## 代理启动检查

- 进入仓库后，优先阅读 `README.md`、`docs/quickstart/QUICK_START.md`、`FIX_NOW.md`、`QUICK_FIX_PERMISSIONS.md`，再开始实现。
- 涉及后端模块边界或依赖关系时，必须先阅读 `docs/module-dependency-and-spi-architecture.md`。
- 涉及前端代码时，先阅读 `frontend/README.md`，并根据任务主题读取 `frontend/.claude/skills/` 下对应 skill 文件后再修改代码。
- 涉及某个业务模块时，优先查看该模块的 `README.md`；若是 `mortise-member` 一类基础业务模块，还要查看 `SERVICE_EXTENSION_GUIDE.md` 之类的扩展说明。

## 专项指令入口

- 后端 Java / Maven 模块边界、分层、SPI 扩展、最小 Maven 校验：见 `.github/instructions/backend-module-boundaries.instructions.md`。
- 前端 Nuxt apps / packages / layers 的边界、类型规则、UI 约束、最小前端校验：见 `.github/instructions/frontend-nuxt-patterns.instructions.md`。
- 启动失败、401/403、CORS、数据库权限、Jasypt、商业模块装配等环境链路排障：优先使用 `.github/prompts/mortise-env-triage.prompt.md`。

## 关键目录速查

- `mortise-app`：Spring Boot 聚合启动模块，负责装配各业务与基础设施模块。
- `mortise-common`、`mortise-core`：最低层公共能力，新增跨模块通用能力时优先考虑放在这里，但避免把业务逻辑下沉进去。
- `mortise-auth`、`mortise-web-support`、`mortise-monitor`：应用基础层；新增能力时先判断是否能用 SPI 或事件扩展，而不是直接互相依赖。
- `mortise-system`、`mortise-member`、`mortise-product`、`mortise-im`：标准业务域模块，通常拆为 `domain/application/infra/admin/api`。
- `frontend/apps/admin`：后台管理端，SPA，开发时依赖相对路径 API 代理。
- `frontend/apps/site`：官网和用户端，当前实际开发端口以应用配置为准。
- `frontend/packages`：共享鉴权、SDK、UI、工程配置；跨 app 共用能力优先放包里而不是复制到 app 内。
- `frontend/layers`：Nuxt layer 扩展点；`base` 常驻，`community`、`commerce` 按需启用。
- `docs/`：架构与专题文档。涉及缓存、安全、OAuth2、数据库、迁移时先搜索对应子目录，不要靠猜。

## 构建与运行

### 后端

- 本项目后端统一使用 **Maven**。
- **始终使用 `mvn`（系统安装的 Maven），禁止使用 `mvnw` 或 `mvnw.cmd`。**
- 常用命令：
  - `mvn clean package -DskipTests`
  - `mvn clean install`
  - `mvn test`
  - `mvn spring-boot:run`
- 需要只验证某个模块时，优先使用 `-pl <module> -am` 缩小范围，例如：`mvn -pl mortise-app -am clean compile -DskipTests`。

### 前端

- 前端位于 `frontend/`，使用 **pnpm workspace**，禁止使用 npm/yarn。
- `frontend/package.json` 已锁定 `pnpm@10.29.3`，修改前端前先确认在 `frontend/` 目录执行命令。
- 常用命令：
  - `pnpm install`
  - `pnpm dev:admin`
  - `pnpm dev:site`
  - `pnpm build`
  - `pnpm lint`
  - `pnpm typecheck`
  - `pnpm typecheck:packages`

## 项目概览

- 这是一个 **Spring Boot 多模块单体** 项目，名称为 **Mortise**。
- 后端基于 **Java 21+**、**Spring Boot 3.5.x**、**MyBatis-Flex**、**PostgreSQL**、**Spring Security OAuth2**。
- 前端是独立 monorepo，基于 **Nuxt 4 + Nuxt UI 4.5 + Pinia + TypeScript**。
- 根目录 `compose.yaml` 启动的是 PostgreSQL、Redis、Logto、Nginx 等基础设施，**不包含** Spring Boot 应用本身。

## 终端环境

- 默认终端是 **PowerShell 7（pwsh）**。
- 运行命令时使用 PowerShell 语法与 cmdlet，不要假设 bash 环境存在。

## 语言约定

- 代码注释、提交信息、文档内容默认使用 **简体中文**，除非用户明确要求其他语言。

## 代码实现偏好

- Java 代码遵循 `java.instructions.md` 与 `springboot.instructions.md`。
- 改动应优先修复根因，不做表面补丁；避免顺手重构无关代码。
- 修改前先确认目标模块或前端 app/package 的现有模式，再保持一致，不要把另一处的设计硬搬过来。

## 代理工作方式建议

- 开始编码前，先确认改动位于后端、前端、文档还是基础设施脚本，再选择对应命令与阅读路径。
- 后端改动完成后，按后端专项 instruction 做最小必要 Maven 校验；前端改动完成后，按前端专项 instruction 做最小必要 lint 或 typecheck。
- 如果某个错误更像环境问题而不是代码问题，优先走 `mortise-env-triage` 这类环境链路排查，而不是立即重构业务代码。
- 用户要求新功能时，优先沿用现有模块和目录模式实现；若发现文档落后于代码，可在完成任务后顺手补文档，但不要先大面积整理文档再编码。
