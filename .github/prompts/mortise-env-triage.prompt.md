---
description: "排查 Mortise 启动失败、401/403、跨域、数据库连接、Jasypt 配置等环境链路问题，先查环境与配置再决定是否改代码"
name: "mortise-env-triage"
argument-hint: "描述症状，例如：mortise-app 启动失败 / admin 跨域 / 401 刷新失败 / PostgreSQL 权限不足"
agent: "agent"
---

针对 Mortise 仓库中的环境、配置与集成问题执行一次**环境优先**排障，不要一上来修改业务代码。

任务目标：根据用户描述的症状，优先沿着环境链路定位根因，给出最小修复建议；只有在环境与配置问题基本排除后，才考虑代码层面的改动。

先阅读并遵循这些仓库文件：
- [工作区说明](../copilot-instructions.md)
- [快速开始](../../docs/quickstart/QUICK_START.md)
- [数据库权限快速修复](../../docs/database/FLYWAY_PERMISSION_FIX.md)
- [PostgreSQL 17 兼容性说明](../../docs/database/POSTGRESQL_17_COMPATIBILITY.md)
- [模块依赖与 SPI 架构](../../docs/module-dependency-and-spi-architecture.md)

排障步骤：
1. 先根据症状分类问题：启动失败、数据库连接/权限、401/403、OAuth2 回调、CORS、前端代理、私有子模块/商业模块、端口或脚本不一致。
2. 优先检查环境与配置，而不是猜业务代码：
   - `ENCRYPTION_KEY` 是否已设置，且与配置中的 `ENC(...)` 匹配。
   - `compose.yaml` 依赖服务是否已启动，尤其是 PostgreSQL、Redis、Logto、Nginx。
   - PostgreSQL 数据库与 schema 权限是否不足，是否需要使用 PowerShell 修复脚本。
   - 前端是否错误设置了 `NUXT_PUBLIC_API_BASE`，导致 admin 绕过本地代理而触发 CORS。
   - 实际端口、脚本、profile 与依赖装配是否以 `package.json`、`nuxt.config.ts`、`pom.xml` 为准，而不是旧文档。
   - 私有或商业模块是否缺失、无权限或未通过 `pro` profile 正确装配。
3. 若问题涉及后端模块装配或依赖关系，检查是否违反了模块边界或错误地引入了同层依赖。
4. 仅在环境与配置问题被基本排除后，才分析代码缺陷，并说明为什么这不是环境问题。

输出格式：

**症状判断**
- 用 1 段话概括你认为最可能的问题类别。

**优先怀疑项**
- 列出 1-3 个最可能根因，按概率排序。

**已核对证据**
- 说明你查看了哪些配置、脚本、代码或日志线索。

**最小修复路径**
- 给出最小可执行修复步骤，优先环境修复、配置修复、脚本修复。

**是否需要改代码**
- 明确回答：`需要` 或 `暂不需要`。
- 若需要，说明为什么环境链路已基本排除，以及建议修改的最小范围。

要求：
- 结论必须结合 Mortise 仓库的真实配置，不要套用泛化 Spring Boot 或 Nuxt 建议。
- 如果文档与代码冲突，优先相信实际配置文件与脚本。
- 如果缺少必要信息，先指出缺口并说明下一步最有价值的检查项。
- 回答要偏操作性，避免长篇原理说明。
