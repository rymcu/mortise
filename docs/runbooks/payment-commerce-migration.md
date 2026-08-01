# Payment Commerce 迁移 Runbook

本文只描述 V441、V442、V443 的受控部署顺序。当前 `192.168.88.147:15432` 的 Flyway
最新成功版本为 V440；在完成审批、备份和维护窗口确认前，不得执行迁移。

## 迁移前检查

从父仓根目录运行只读检查：

```powershell
pwsh -File ./scripts/payment-migration-preflight.ps1
```

脚本通过 SSH 在 `88.147` 的 `mortise-postgres` 容器中执行只读 SQL，并检查：

- `mortise.flyway_schema_history` 最新成功版本必须是 V440；
- V441、V442、V443 不得已有成功记录；
- V440 已创建的 member session 表不存在 V441 新增的 `scope` 列；V442 Inbox 表和 V443 shadow 审计表均不存在；
- 三个待执行 SQL 文件存在，并输出 SHA-256 供发布记录留档。

脚本失败即停止。它不执行 `flyway migrate`、DDL、服务重启、Rabbit 拓扑变更或消费者启用。

## 分阶段应用

每一阶段都必须使用能限制 Flyway target 的发布任务，并在应用启动后重新执行只读核验。
不要一次性把三份迁移和 shadow listener 同时发布。

1. 发布只包含 V441 的版本，设置 Flyway target 为 `441`，确认 Flyway 成功记录为 V441，
   并确认 `mortise_member_client_session.scope`、相关索引和权限符合 V441 预期。
2. 发布包含 V442 的版本，设置 target 为 `442`，确认 Inbox 表、主键
   `(event_id, consumer_name)`、状态约束和索引存在。此时 Commerce 正式 listener 与 shadow
   listener 仍保持关闭。
3. 发布包含 V443 的版本，设置 target 为 `443`，确认 shadow 审计表、payload hash 约束和
   `(event_id, shadow_consumer_name)` 幂等键存在。审计表不得包含原始支付载荷。
4. 在应用配置中注入可信 tenant、Rabbit 凭据和 feature flag，但先保持两个 listener 的
   `autoStartup=false`，完成连接、passive declare、健康检查和权限核验。
5. 只启用 shadow listener，观察 MATCH/MISMATCH/NOT_FOUND/INVALID 结果和 shadow DLQ；
   shadow 不得更新订单、创建仓库授权、写正式 Inbox 或发布 retry。
6. shadow 观察窗通过后，才可按 canary 方案启用正式 Commerce listener。新旧订单必须按
   owner 路由，禁止同一订单同时由 Spring Event 和 RabbitMQ 驱动。

## 回滚与停止条件

- Flyway 没有安全的通用 down migration。V441/V442/V443 应视为已应用事实，失败时停止新
  流量、保留表和审计数据，修复应用版本后继续，不执行手工删表或修改历史 checksum。
- 任一阶段发现 checksum 不一致、非预期已应用版本、跨租户数据、shadow DLQ 增长、payload
  hash 冲突或订单副作用，立即关闭 shadow/正式 listener，并保留 Rabbit 队列消息供审计。
- 关闭 listener 不等于删除队列或清空 Inbox/DLQ。任何重放必须记录操作人、原因、eventId、
  payload hash 和结果。
- 若新网关已拥有订单，回滚只能停止新订单流量；不得把该订单倒灌旧 Payment 或让旧
  Spring Event 再次驱动同一订单。

## 生产前未完成项

V441/V442/V443 正式部署前，仍需完成 Provider sandbox/真实签名样本、OAuth2 可信租户解析、
退款并发与累计额度、Rabbit TLS/三节点 quorum HA、备份恢复、告警、DLX quorum 故障注入和
7 天 shadow/canary 观察。
