# Mortise 文档

`docs/` 目录仅保留两类内容：

- 主文档：本文件
- 用户手册：安装、启动、配置、接入、部署、排障相关说明

设计方案、需求文档、阶段报告、重构总结、实现状态、示例脚本等内部资料已从该目录移除。

## 文档索引

### 快速开始
- [quickstart/QUICK_START.md](quickstart/QUICK_START.md)
- [quickstart/FRONTEND_QUICK_START.md](quickstart/FRONTEND_QUICK_START.md)
- [quickstart/AI_ASSISTED_DEVELOPMENT.md](quickstart/AI_ASSISTED_DEVELOPMENT.md)
- [quickstart/COMMERCIAL_MODULE_DEVELOPMENT.md](quickstart/COMMERCIAL_MODULE_DEVELOPMENT.md)
- [quickstart/COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md](quickstart/COMMERCIAL_MODULE_BACKEND_DEVELOPMENT.md)
- [quickstart/COMMERCIAL_MODULE_FRONTEND_LAYER.md](quickstart/COMMERCIAL_MODULE_FRONTEND_LAYER.md)

### 数据库与初始化
- [database/AUTO_TABLE_CREATION_AND_INIT_GUIDE.md](database/AUTO_TABLE_CREATION_AND_INIT_GUIDE.md)
- [database/DATABASE_PERMISSION_EXPLAINED.md](database/DATABASE_PERMISSION_EXPLAINED.md)

### 监控
- [monitoring/actuator-access-guide.md](monitoring/actuator-access-guide.md)

### OAuth2
- [oauth2/OAUTH2_QUICK_START.md](oauth2/OAUTH2_QUICK_START.md)
- [oauth2/DYNAMIC_OAUTH2_CLIENT_GUIDE.md](oauth2/DYNAMIC_OAUTH2_CLIENT_GUIDE.md)
- [oauth2/oauth2-configuration-guide.md](oauth2/oauth2-configuration-guide.md)
- [oauth2/oauth2-dual-logto-configuration.md](oauth2/oauth2-dual-logto-configuration.md)
- [oauth2/oauth2-binding-quick-reference.md](oauth2/oauth2-binding-quick-reference.md)
- [oauth2/oauth2-binding-usage-examples.md](oauth2/oauth2-binding-usage-examples.md)

### 安全
- [security/security-configuration-guide.md](security/security-configuration-guide.md)
- [security/USER_CONTEXT_SERVICE_USAGE.md](security/USER_CONTEXT_SERVICE_USAGE.md)

### 微信
- [wechat/WECHAT_QUICK_START.md](wechat/WECHAT_QUICK_START.md)
- [wechat/WECHAT_DEPLOYMENT_GUIDE.md](wechat/WECHAT_DEPLOYMENT_GUIDE.md)
- [wechat/WECHAT_DATABASE_CONFIG.md](wechat/WECHAT_DATABASE_CONFIG.md)
- [wechat/WECHAT_OAUTH2_INTEGRATION_GUIDE.md](wechat/WECHAT_OAUTH2_INTEGRATION_GUIDE.md)
- [wechat/wechat-startup-troubleshooting.md](wechat/wechat-startup-troubleshooting.md)
- [wechat/wechat-token-client-quick-reference.md](wechat/wechat-token-client-quick-reference.md)

## 使用原则

- 新增 `docs/` 内容时，只放面向使用者的手册和操作说明。
- 设计稿、方案、评审记录、阶段性报告不再进入该目录。
- 新文档需要同步更新本索引。
