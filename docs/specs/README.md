# 需求规范中心 (spec-kit)

本目录用于沉淀规范驱动研发的需求文档，确保新业务以“规范先行 → 设计评审 → 实现”推进。

## 目录结构

- templates/
  - feature-spec.md (规范模板)
  - feature-spec-lite.md (轻量规范模板)
- WORKFLOW.md (规范落地流程)
- INDEX.md (规范索引)

## 命名规则

- 需求规范命名: YYYY-MM-DD-<short-topic>.md
- 示例: 2026-02-10-member-login-otp.md

## 状态字段

规范正文中需包含状态字段:
- Draft: 草稿
- Review: 评审中
- Approved: 已批准
- Implemented: 已实现
- Deprecated: 已废弃

## 域/模块速查

- L1 核心层: mortise-common, mortise-core
- L2 基础设施层: mortise-log, mortise-cache, mortise-notification, mortise-persistence
- L3 应用基础层: mortise-auth, mortise-web-support, mortise-monitor
- L4/L5 业务域 (system): mortise-system-domain, mortise-system-application, mortise-system-infra, mortise-system-admin, mortise-system-api
- L4/L5 业务域 (member): mortise-member-domain, mortise-member-application, mortise-member-infra, mortise-member-admin, mortise-member-api
- 其他: mortise-wechat, mortise-test-support, mortise-app

分层与依赖规则详见 [docs/architecture/architecture.md](../architecture/architecture.md)

## 快速开始

1. 复制模板: templates/feature-spec.md
2. 填写并提交 PR, 进入评审
3. 通过评审后, 再进入实现 PR

## 索引维护

运行以下命令生成规范索引:

```bash
pwsh ./docs/scripts/generate-spec-index.ps1
```

更多细则见 WORKFLOW.md
