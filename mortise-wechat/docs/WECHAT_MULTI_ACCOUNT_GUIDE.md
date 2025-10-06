# 微信多账号配置指南

## 概述

从 v1.0 开始，mortise-wechat 模块支持**多账号管理**，允许同时配置和使用多个微信公众号、开放平台账号等。

## 架构设计

### 数据库表结构

#### 1. mortise_wechat_account（账号表）

存储微信账号的基本信息：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_type | VARCHAR(20) | 账号类型：mp(公众号)、open(开放平台)、miniapp(小程序) |
| account_name | VARCHAR(100) | 账号名称（用于识别） |
| app_id | VARCHAR(50) | 微信 AppID |
| app_secret | VARCHAR(200) | 微信 AppSecret（加密存储） |
| is_default | SMALLINT | 是否默认账号（每种类型只能有一个默认账号） |
| is_enabled | SMALLINT | 是否启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 2. mortise_wechat_config（配置表）

存储每个账号的具体配置：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_id | BIGINT | 关联的账号ID（外键） |
| config_key | VARCHAR(50) | 配置键（如：token、aesKey、redirectUri） |
| config_value | TEXT | 配置值 |
| is_encrypted | SMALLINT | 是否加密存储 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 服务架构

```
WeChatMultiAccountConfigService (配置加载服务)
├── loadDefaultMpConfig()         # 加载默认公众号配置
├── loadMpConfigByAccountId()     # 根据账号ID加载公众号配置
├── loadMpConfigByAppId()         # 根据AppID加载公众号配置
├── loadDefaultOpenConfig()       # 加载默认开放平台配置
├── loadOpenConfigByAccountId()   # 根据账号ID加载开放平台配置
├── getAllMpAccounts()            # 获取所有公众号账号
└── getAllOpenAccounts()          # 获取所有开放平台账号

WeChatAccountManagementService (账号管理服务)
├── createAccount()               # 创建账号
├── updateAccount()               # 更新账号
├── deleteAccount()               # 删除账号
├── setDefaultAccount()           # 设置默认账号
├── toggleAccount()               # 启用/禁用账号
├── saveConfig()                  # 保存配置
├── deleteConfig()                # 删除配置
└── refreshCache()                # 刷新缓存

WeChatMpConfiguration (公众号配置类)
├── wxMpService()                 # 默认公众号服务
└── wxMpServiceMap()              # 多账号服务映射

WeChatOpenConfiguration (开放平台配置类)
├── wxOpenService()               # 默认开放平台服务
└── wxOpenServiceMap()            # 多账号服务映射
```

## 使用指南

### 1. 创建微信账号

**API 接口：**

```bash
POST /api/wechat/admin/accounts
Content-Type: application/json

{
  "accountType": "mp",                    # mp/open/miniapp
  "accountName": "官方公众号",
  "appId": "wx1234567890abcdef",
  "appSecret": "your-app-secret",
  "isDefault": true,                      # 是否设为默认
  "isEnabled": true                       # 是否启用
}
```

**使用 Service：**

```java
@Autowired
private WeChatAccountManagementService accountManagementService;

WeChatAccount account = new WeChatAccount();
account.setAccountType("mp");
account.setAccountName("官方公众号");
account.setAppId("wx1234567890abcdef");
account.setAppSecret("your-app-secret");
account.setIsDefault(1);
account.setIsEnabled(1);

Long accountId = accountManagementService.createAccount(account);
```

### 2. 配置账号参数

**API 接口：**

```bash
POST /api/wechat/admin/accounts/{accountId}/configs
Content-Type: application/json

{
  "configKey": "token",
  "configValue": "your-token-value",
  "isEncrypted": false
}

# 敏感配置加密存储
{
  "configKey": "aesKey",
  "configValue": "your-aes-key",
  "isEncrypted": true
}
```

**常用配置项：**

| 配置键 | 说明 | 是否加密 |
|--------|------|----------|
| token | 消息验证Token | false |
| aesKey | 消息加密密钥 | true |
| redirectUri | OAuth2回调地址 | false |
| qrCodeExpireSeconds | 二维码过期时间（秒） | false |

### 3. 使用默认账号

**公众号服务：**

```java
@Autowired
private WxMpService wxMpService;  // 自动注入默认账号服务

// 发送模板消息
WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
    .toUser(openId)
    .templateId(templateId)
    .build();
    
wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
```

**开放平台服务：**

```java
@Autowired
private WxOpenService wxOpenService;  // 自动注入默认账号服务

// 生成授权链接
String authUrl = wxOpenService.buildAuthorizationUrl(redirectUri, scope, state);
```

### 4. 使用指定账号

**方式一：通过账号ID**

```java
@Autowired
private Map<Long, WxMpService> wxMpServiceMap;

// 使用账号ID为 1 的公众号
Long accountId = 1L;
WxMpService service = wxMpServiceMap.get(accountId);

if (service != null) {
    // 发送消息
    service.getKefuService().sendKefuMessage(message);
}
```

**方式二：通过配置服务动态获取**

```java
@Autowired
private WeChatMultiAccountConfigService configService;

// 根据 AppID 获取配置
WeChatMpProperties properties = configService.loadMpConfigByAppId("wx1234567890abcdef");

// 或根据账号ID获取配置
WeChatMpProperties properties = configService.loadMpConfigByAccountId(1L);
```

### 5. 账号管理操作

**设置默认账号：**

```bash
POST /api/wechat/admin/accounts/{accountId}/set-default
```

```java
accountManagementService.setDefaultAccount(accountId);
```

**启用/禁用账号：**

```bash
POST /api/wechat/admin/accounts/{accountId}/toggle?enabled=true
```

```java
accountManagementService.toggleAccount(accountId, true);
```

**获取账号列表：**

```bash
GET /api/wechat/admin/accounts?accountType=mp
```

```java
List<WeChatAccount> mpAccounts = accountManagementService.getAccounts("mp");
```

**删除账号：**

```bash
DELETE /api/wechat/admin/accounts/{accountId}
```

```java
accountManagementService.deleteAccount(accountId);
```

### 6. 刷新配置缓存

修改数据库配置后，需要刷新缓存：

```bash
POST /api/wechat/admin/refresh-cache
```

```java
accountManagementService.refreshCache();
```

## 数据初始化

### 初始化 SQL 示例

```sql
-- 1. 创建公众号账号
INSERT INTO mortise_wechat_account 
(account_type, account_name, app_id, app_secret, is_default, is_enabled)
VALUES 
('mp', '官方公众号', 'wx1234567890abcdef', 'ENC(encrypted_secret)', 1, 1),
('mp', '测试公众号', 'wx0987654321fedcba', 'ENC(encrypted_secret)', 0, 1);

-- 2. 配置第一个账号（假设 ID 为 1）
INSERT INTO mortise_wechat_config 
(account_id, config_key, config_value, is_encrypted)
VALUES 
(1, 'token', 'your_token_here', 0),
(1, 'aesKey', 'ENC(encrypted_aes_key)', 1);

-- 3. 创建开放平台账号
INSERT INTO mortise_wechat_account 
(account_type, account_name, app_id, app_secret, is_default, is_enabled)
VALUES 
('open', '默认开放平台', 'wxopen123456', 'ENC(encrypted_secret)', 1, 1);

-- 4. 配置开放平台
INSERT INTO mortise_wechat_config 
(account_id, config_key, config_value, is_encrypted)
VALUES 
(3, 'redirectUri', 'https://yourdomain.com/wechat/callback', 0),
(3, 'qrCodeExpireSeconds', '300', 0);
```

## 应用场景

### 场景一：多公众号管理

企业有多个公众号（如：客服号、营销号、官方号），需要集中管理：

```java
// 从客服号发送消息
WxMpService customerService = wxMpServiceMap.get(customerAccountId);
customerService.getKefuService().sendKefuMessage(...);

// 从营销号发送模板消息
WxMpService marketingService = wxMpServiceMap.get(marketingAccountId);
marketingService.getTemplateMsgService().sendTemplateMsg(...);
```

### 场景二：多环境配置

开发、测试、生产环境使用不同的微信账号：

```java
// 根据环境变量选择账号
String env = System.getenv("APP_ENV");
Long accountId = env.equals("prod") ? prodAccountId : testAccountId;

WeChatMpProperties properties = configService.loadMpConfigByAccountId(accountId);
```

### 场景三：租户隔离（SaaS 模式）

为每个租户配置独立的微信账号：

```java
// 根据租户ID获取对应的微信账号
Long tenantId = getCurrentTenantId();
Long accountId = getAccountIdByTenant(tenantId);

WxMpService service = wxMpServiceMap.get(accountId);
```

## 配置缓存

### 缓存策略

- **缓存名称：** `wechat:config`
- **缓存键格式：**
  - 公众号默认配置：`mp:default`
  - 公众号指定账号：`mp:{accountId}`
  - 开放平台默认配置：`open:default`
  - 开放平台指定账号：`open:{accountId}`

### 缓存失效场景

自动失效：
- 创建/更新/删除账号
- 保存/删除配置
- 设置默认账号
- 启用/禁用账号

手动失效：
```java
accountManagementService.refreshCache();
```

## 安全建议

1. **敏感信息加密**
   - AppSecret 始终加密存储
   - AESKey 加密存储
   - 使用 Jasypt 进行加密

2. **权限控制**
   - 账号管理接口仅限管理员访问
   - 添加认证和授权机制

3. **审计日志**
   - 记录账号创建、修改、删除操作
   - 记录配置变更历史

4. **数据备份**
   - 定期备份账号和配置数据
   - 加密存储敏感配置的备份

## 常见问题

### Q1: 如何切换默认账号？

```java
accountManagementService.setDefaultAccount(newAccountId);
accountManagementService.refreshCache();
```

### Q2: 如何临时禁用某个账号？

```java
accountManagementService.toggleAccount(accountId, false);
```

### Q3: 多账号时如何路由到正确的账号？

可以根据业务场景选择路由策略：
- **按 AppID 路由：** 微信回调时携带 AppID，根据 AppID 选择账号
- **按租户路由：** 根据当前用户所属租户选择对应账号
- **按功能路由：** 不同功能使用不同账号（如客服、营销）

### Q4: 配置缓存什么时候刷新？

自动刷新场景：
- 任何账号或配置的增删改操作
- 不需要手动刷新

手动刷新场景：
- 直接修改数据库后
- 需要强制重新加载配置时

## 迁移指南

### 从单账号迁移到多账号

1. **数据迁移**

```sql
-- 将原有配置迁移到新表结构
-- 1. 创建账号
INSERT INTO mortise_wechat_account (account_type, account_name, app_id, app_secret, is_default, is_enabled)
SELECT 
    config_type,
    CONCAT(config_type, '默认账号'),
    config_value,
    (SELECT config_value FROM mortise_wechat_config_old WHERE config_type = 'mp' AND config_key = 'appSecret'),
    1,
    1
FROM mortise_wechat_config_old
WHERE config_key = 'appId'
GROUP BY config_type;

-- 2. 迁移配置
INSERT INTO mortise_wechat_config (account_id, config_key, config_value, is_encrypted)
SELECT 
    a.id,
    old.config_key,
    old.config_value,
    old.is_encrypted
FROM mortise_wechat_config_old old
JOIN mortise_wechat_account a ON old.config_type = a.account_type
WHERE old.config_key NOT IN ('appId', 'appSecret');
```

2. **代码更新**

将原有的 `WeChatConfigService` 替换为 `WeChatMultiAccountConfigService`：

```java
// 旧代码
@Autowired
private WeChatConfigService configService;
WeChatMpProperties properties = configService.loadMpConfig();

// 新代码
@Autowired
private WeChatMultiAccountConfigService configService;
WeChatMpProperties properties = configService.loadDefaultMpConfig();
```

## 参考资料

- [WxJava 官方文档](https://github.com/Wechat-Group/WxJava/wiki)
- [微信公众平台开发文档](https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html)
- [微信开放平台开发文档](https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html)

## 更新日志

### v1.0.0 (2024-01-XX)

- ✅ 支持多微信账号管理
- ✅ 支持默认账号设置
- ✅ 支持账号启用/禁用
- ✅ 配置缓存优化
- ✅ 账号管理 REST API
- ✅ 敏感信息加密存储
