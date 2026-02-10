# 需求规范: <标题>

- 状态: Draft
- Owner: <姓名>
- 业务域: <system/member/wechat/cross-cutting>
- 归属层级: <L1-L6>
- 影响模块: <模块清单>
- API 类型: <admin/api/none>
- 关联需求: <任务号/链接>
- 评审者: <姓名列表>
- 目标上线时间: <YYYY-MM-DD>
- 模块负责人: <Owner 映射/审批人>

## 1. 架构归属

- 业务域归属与模块边界
- 依赖约束 (同层不依赖, L5 仅依赖 L3-L4 与本域)
- API 入口位置 (admin/api)

### 模块选择参考

- L1: mortise-common, mortise-core
- L2: mortise-log, mortise-cache, mortise-notification, mortise-persistence
- L3: mortise-auth, mortise-web-support, mortise-monitor
- system: mortise-system-domain, mortise-system-application, mortise-system-infra, mortise-system-admin, mortise-system-api
- member: mortise-member-domain, mortise-member-application, mortise-member-infra, mortise-member-admin, mortise-member-api
- 其他: mortise-wechat, mortise-test-support, mortise-app

### 责任归属参考

- system: <系统域 Owner/审批人>
- member: <会员域 Owner/审批人>
- wechat: <微信域 Owner/审批人>
- cross-cutting: <基础设施/平台 Owner/审批人>

## 2. 背景与目标

- 背景问题
- 业务目标
- 不做什么 (非目标)

## 3. 需求范围

- 用户故事
- 功能边界
- 依赖与前置条件

## 4. 方案设计

- 关键流程
- 接口设计 (请求/响应/错误码)
- 权限与安全要求

## 5. 数据与存储

- 数据模型变化
- 迁移策略
- 兼容性与回滚

## 6. 影响评估

- 现有功能影响
- 性能与容量
- 监控与告警

## 7. 里程碑

- 评审完成
- 开发完成
- 联调完成
- 验收完成

## 8. 验收标准

- 可执行的验收条目
- 测试用例或脚本链接

## 9. 变更记录

- YYYY-MM-DD: 变更摘要
