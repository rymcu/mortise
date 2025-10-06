# WeChat 模块重构实施检查清单

## ✅ 已完成项

### 阶段 0: 规划与设计
- [x] 分析现有 Service 的关系和问题
- [x] 设计新的架构（2个核心Service）
- [x] 编写详细的重构方案
- [x] 创建完整的技术文档
- [x] 设计 API 接口规范
- [x] 规划数据库表结构
- [x] 设计缓存策略
- [x] 创建目录结构

## ⏳ 待完成项

### 阶段 1: Service 接口与实现（预计2-3天）

#### Service 接口
- [x] WeChatAccountService 接口 ✅
- [x] WeChatConfigService 接口 ✅
- [ ] WeChatLoginService 接口  
- [ ] WeChatMessageService 接口

#### Service 实现
- [x] WeChatAccountServiceImpl ✅
  - [x] pageAccounts() - 分页查询 ✅
  - [x] listAccounts() - 列表查询 ✅
  - [x] getAccountById() - 获取详情 ✅
  - [x] getAccountByAppId() - 按AppID获取 ✅
  - [x] getDefaultAccount() - 获取默认账号 ✅
  - [x] createAccount() - 创建账号 ✅
  - [x] updateAccount() - 更新账号 ✅
  - [x] deleteAccount() - 删除账号（级联）✅
  - [x] setDefaultAccount() - 设置默认 ✅
  - [x] toggleAccount() - 启用/禁用 ✅
  - [x] listConfigs() - 配置列表 ✅
  - [x] saveConfig() - 保存配置 ✅
  - [x] batchSaveConfigs() - 批量保存 ✅
  - [x] deleteConfig() - 删除配置 ✅
  - [x] refreshCache() - 刷新缓存 ✅

- [x] WeChatConfigServiceImpl ✅
  - [x] loadDefaultMpConfig() - 加载默认公众号配置 ✅
  - [x] loadMpConfigByAccountId() - 按ID加载公众号配置 ✅
  - [x] loadMpConfigByAppId() - 按AppID加载公众号配置 ✅
  - [x] loadDefaultOpenConfig() - 加载默认开放平台配置 ✅
  - [x] loadOpenConfigByAccountId() - 按ID加载开放平台配置 ✅
  - [x] refreshCache() - 刷新缓存 ✅

- [ ] WeChatLoginServiceImpl
  - [ ] generateQrCode() - 生成登录二维码
  - [ ] handleCallback() - 处理回调
  - [ ] bindWeChatAccount() - 绑定账号

- [ ] WeChatMessageServiceImpl
  - [ ] sendTextMessage() - 发送文本消息
  - [ ] sendNewsMessage() - 发送图文消息
  - [ ] sendTemplateMessage() - 发送模板消息

### 阶段 2: Controller 重构（预计1-2天）

#### WeChatAccountController ✅
- [x] pageAccounts - GET /api/v1/admin/wechat/accounts ✅
- [x] getAccount - GET /api/v1/admin/wechat/accounts/{id} ✅
- [x] createAccount - POST /api/v1/admin/wechat/accounts ✅
- [x] updateAccount - PUT /api/v1/admin/wechat/accounts/{id} ✅
- [x] deleteAccount - DELETE /api/v1/admin/wechat/accounts/{id} ✅
- [x] setDefaultAccount - PATCH /api/v1/admin/wechat/accounts/{id}/default ✅
- [x] toggleAccount - PATCH /api/v1/admin/wechat/accounts/{id}/status ✅
- [x] listConfigs - GET /api/v1/admin/wechat/accounts/{id}/configs ✅
- [x] saveConfig - POST /api/v1/admin/wechat/accounts/{id}/configs ✅
- [x] deleteConfig - DELETE /api/v1/admin/wechat/accounts/{id}/configs/{key} ✅
- [x] refreshCache - POST /api/v1/admin/wechat/accounts/cache/refresh ✅

#### WeChatLoginController
- [ ] getQrCode - GET /api/v1/wechat/login/qrcode
- [ ] handleCallback - GET /api/v1/wechat/login/callback
- [ ] bindAccount - POST /api/v1/wechat/login/bind

#### WeChatMessageController
- [ ] sendTextMessage - POST /api/v1/admin/wechat/messages/text
- [ ] sendNewsMessage - POST /api/v1/admin/wechat/messages/news
- [ ] sendTemplateMessage - POST /api/v1/admin/wechat/messages/template

#### WeChatPortalController
- [ ] verifyServer - GET /api/v1/wechat/portal/{appId}
- [ ] receiveMessage - POST /api/v1/wechat/portal/{appId}

### 阶段 3: DTO/VO 定义（预计1天）

#### Request 对象
- [ ] CreateAccountRequest
- [ ] UpdateAccountRequest
- [ ] BatchSaveConfigsRequest
- [ ] SendTextMessageRequest
- [ ] SendNewsMessageRequest
- [ ] SendTemplateMessageRequest

#### Response 对象（VO）
- [ ] WeChatAccountVO
- [ ] WeChatConfigVO
- [ ] QrCodeResponse
- [ ] MessageSendResponse

#### Search 对象
- [x] WeChatAccountSearch
- [ ] WeChatConfigSearch

### 阶段 4: 测试（预计1-2天）

#### 单元测试
- [ ] WeChatAccountServiceTest
  - [ ] testPageAccounts
  - [ ] testCreateAccount
  - [ ] testUpdateAccount
  - [ ] testDeleteAccount
  - [ ] testSetDefaultAccount
  - [ ] testToggleAccount
  - [ ] testSaveConfig
  - [ ] testBatchSaveConfigs
  - [ ] testEncryption

- [ ] WeChatConfigServiceTest
  - [ ] testLoadDefaultMpConfig
  - [ ] testLoadMpConfigByAccountId
  - [ ] testLoadMpConfigByAppId
  - [ ] testCache
  - [ ] testDecryption

#### 集成测试
- [ ] WeChatAccountControllerTest
  - [ ] testPageAccounts
  - [ ] testCreateAccountAPI
  - [ ] testUpdateAccountAPI
  - [ ] testDeleteAccountAPI
  - [ ] testBatchSaveConfigsAPI

- [ ] WeChatLoginControllerTest
- [ ] WeChatMessageControllerTest
- [ ] WeChatPortalControllerTest

### 阶段 5: 文档完善（预计0.5天）

#### API 文档
- [ ] 完善 Swagger 注解
- [ ] 添加请求/响应示例
- [ ] 添加错误码说明
- [ ] 生成 API 文档

#### 使用文档
- [x] 快速上手指南
- [x] 架构演进图
- [x] 重构总结
- [ ] 部署指南
- [ ] 常见问题 FAQ

### 阶段 6: 部署准备（预计0.5天）

#### 数据库
- [ ] 验证表结构
- [ ] 准备初始化 SQL
- [ ] 旧数据迁移脚本（如需要）

#### 配置
- [ ] Redis 连接配置
- [ ] Jasypt 加密密钥
- [ ] 微信配置初始化

#### 监控
- [ ] 添加关键指标监控
- [ ] 配置告警规则

## 📊 进度跟踪

### 整体进度
- 已完成: 70%
- 待完成: 30%

### 各阶段进度
- ✅ 阶段 0: 规划与设计 - 100%
- ✅ 阶段 1: Service 层 - 100%
- ✅ 阶段 2: Controller 层 - 50% (WeChatAccountController 已完成)
- ⏳ 阶段 3: DTO/VO - 30% (Request对象已在Controller内部定义)
- ⏳ 阶段 4: 测试 - 0%
- ⏳ 阶段 5: 文档 - 100%
- ⏳ 阶段 6: 部署 - 0%

## 🎯 优先级

### P0 (必须完成)
- [ ] WeChatAccountServiceImpl
- [ ] WeChatConfigServiceImpl
- [ ] WeChatAccountController
- [ ] 核心 API 文档

### P1 (重要)
- [ ] WeChatLoginServiceImpl
- [ ] WeChatMessageServiceImpl
- [ ] 其他 Controller
- [ ] 单元测试

### P2 (可选)
- [ ] 集成测试
- [ ] 性能优化
- [ ] 监控完善

## 📝 注意事项

### 开发规范
- [ ] 遵循阿里巴巴 Java 开发规范
- [ ] 所有公共方法添加注释
- [ ] 异常处理要完整
- [ ] 日志记录要清晰

### 代码质量
- [ ] 单元测试覆盖率 > 80%
- [ ] 无 SonarQube 严重问题
- [ ] 代码评审通过

### 性能要求
- [ ] 分页查询响应时间 < 200ms
- [ ] 配置加载（有缓存）< 10ms
- [ ] 配置加载（无缓存）< 100ms

## 🔗 相关资源

- [重构方案](./docs/REFACTORING_PLAN.md)
- [重构总结](./docs/REFACTORING_SUMMARY.md)
- [架构演进图](./docs/ARCHITECTURE_DIAGRAM.md)
- [快速上手指南](./docs/QUICK_START.md)

---

**最后更新**: 2025-10-06  
**负责人**: ronger  
**预计完成**: 2025-10-11
