# Mortise WeChat Integration

微信集成模块，支持微信公众号消息推送和网站扫码登录，**支持多账号管理**。

## 功能特性

### 1. 微信扫码登录
- ✅ PC 端扫码登录（微信开放平台）
- ✅ H5 授权登录
- ✅ 获取用户基本信息
- ✅ Access Token 管理

### 2. 微信公众号消息推送
- ✅ 模板消息推送
- ✅ 客服消息（文本、图片、图文）
- ✅ 消息管理

### 3. 多账号管理 🆕
- ✅ 支持多个微信公众号账号
- ✅ 支持多个开放平台账号
- ✅ 数据库动态配置
- ✅ 默认账号设置
- ✅ 账号启用/禁用
- ✅ 敏感信息加密存储
- ✅ 配置缓存优化
- ✅ 账号管理 REST API

## 快速开始

### 1. 添加依赖

在 `mortise-app` 模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-wechat</artifactId>
</dependency>
```

### 2. 执行数据库迁移

运行 Flyway 迁移脚本初始化表结构：

```sql
-- 自动执行
resources/db/migration/V1__Create_WeChat_Multi_Account_Tables.sql
```

### 3. 配置微信账号

#### 方式一：使用 REST API（推荐）

```bash
# 创建公众号账号
POST /api/wechat/admin/accounts
Content-Type: application/json

{
  "accountType": "mp",
  "accountName": "官方公众号",
  "appId": "wx1234567890abcdef",
  "appSecret": "your-app-secret",
  "isDefault": true,
  "isEnabled": true
}

# 添加配置
POST /api/wechat/admin/accounts/{accountId}/configs
{
  "configKey": "token",
  "configValue": "your-token",
  "isEncrypted": false
}

POST /api/wechat/admin/accounts/{accountId}/configs
{
  "configKey": "aesKey",
  "configValue": "your-aes-key",
  "isEncrypted": true
}
```

#### 方式二：直接插入数据库

```sql
-- 创建公众号账号
INSERT INTO mortise_wechat_account 
(account_type, account_name, app_id, app_secret, is_default, is_enabled)
VALUES 
('mp', '官方公众号', 'wx1234567890abcdef', 'ENC(encrypted_secret)', 1, 1);

-- 添加配置（假设账号ID为1）
INSERT INTO mortise_wechat_config 
(account_id, config_key, config_value, is_encrypted)
VALUES 
(1, 'token', 'your_token', 0),
(1, 'aesKey', 'ENC(encrypted_aes_key)', 1);

-- 创建开放平台账号
INSERT INTO mortise_wechat_account 
(account_type, account_name, app_id, app_secret, is_default, is_enabled)
VALUES 
('open', '默认开放平台', 'wxopen123456', 'ENC(encrypted_secret)', 1, 1);

INSERT INTO mortise_wechat_config 
(account_id, config_key, config_value, is_encrypted)
VALUES 
(2, 'redirectUri', 'https://yourdomain.com/wechat/callback', 0),
(2, 'qrCodeExpireSeconds', '300', 0);
```

### 4. 启用微信功能

在 `application.yml` 中启用：

```yaml
wechat:
  mp:
    enabled: true    # 启用公众号功能
  open:
    enabled: true    # 启用开放平台功能
```

### 5. 获取微信参数

#### 微信公众号
1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 开发 -> 基本配置
3. 获取 AppID、AppSecret
4. 设置 Token 和 EncodingAESKey

#### 微信开放平台（扫码登录）
1. 登录 [微信开放平台](https://open.weixin.qq.com/)
2. 管理中心 -> 网站应用
3. 创建网站应用并获取 AppID、AppSecret
4. 配置授权回调域名

## 使用示例

### 1. 微信扫码登录

#### 使用默认账号

```java
@RestController
@RequiredArgsConstructor
public class LoginController {
    
    private final WeChatLoginService weChatLoginService;
    
    // 前端获取登录二维码 URL
    @GetMapping("/login/wechat/qrcode")
    public String getQrCodeUrl() {
        String redirectUri = "http://your-domain.com/api/wechat/login/callback";
        String state = UUID.randomUUID().toString();
        return weChatLoginService.buildAuthorizationUrl(redirectUri, state);
    }
    
    // 处理微信回调
    @GetMapping("/api/wechat/login/callback")
    public String callback(@RequestParam String code, @RequestParam String state) {
        try {
            WxOAuth2UserInfo userInfo = weChatLoginService.getUserInfoByCode(code);
            
            // 1. 根据 openId 或 unionId 查找或创建用户
            // 2. 生成 JWT Token
            // 3. 重定向到前端页面并携带 Token
            
            return "redirect:http://your-frontend.com/login-success?token=" + token;
        } catch (WxErrorException e) {
```
            return "redirect:http://your-frontend.com/login-fail";
        }
    }
}
```

#### H5 授权登录

```java
@GetMapping("/login/wechat/h5")
public String h5Login() {
    String redirectUri = "http://your-domain.com/api/wechat/login/callback";
    String state = UUID.randomUUID().toString();
    
    // 直接重定向到微信授权页面
    return "redirect:" + weChatLoginService.buildH5AuthorizationUrl(redirectUri, state);
}
```

### 2. 发送模板消息

```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final WeChatMessageService weChatMessageService;
    
    public void sendWelcomeMessage(String openId, String username) {
        TemplateMessage message = TemplateMessage.builder()
                .toUser(openId)
                .templateId("your-template-id")
                .url("http://your-domain.com/welcome")
                .build()
                .addData("first", "欢迎注册！")
                .addData("keyword1", username)
                .addData("keyword2", LocalDateTime.now().toString())
                .addData("remark", "感谢您的使用", "#173177");
        
        try {
            String msgId = weChatMessageService.sendTemplateMessage(message);
            log.info("发送欢迎消息成功，msgId: {}", msgId);
        } catch (WxErrorException e) {
            log.error("发送欢迎消息失败", e);
        }
    }
}
```

### 3. 发送客服消息

```java
@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final WeChatMessageService weChatMessageService;
    
    // 发送文本消息
    public void sendText(String openId, String content) throws WxErrorException {
        weChatMessageService.sendTextMessage(openId, content);
    }
    
    // 发送图文消息
    public void sendNews(String openId) throws WxErrorException {
        weChatMessageService.sendNewsMessage(
            openId,
            "标题",
            "描述",
            "http://your-domain.com/article/1",
            "http://your-domain.com/images/cover.jpg"
        );
    }
}
```

## API 接口

### 登录相关

#### 获取 PC 扫码登录 URL
```
GET /api/wechat/login/qrcode-url?redirectUri=http://your-domain.com/callback
```

响应：
```json
{
  "authUrl": "https://open.weixin.qq.com/connect/qrconnect?...",
  "state": "uuid-string"
}
```

#### 获取 H5 授权 URL
```
GET /api/wechat/login/h5-url?redirectUri=http://your-domain.com/callback
```

#### 授权回调
```
GET /api/wechat/login/callback?code=xxx&state=xxx
```

### 消息推送相关

#### 发送模板消息
```
POST /api/wechat/message/template
Content-Type: application/json

{
  "toUser": "openid",
  "templateId": "template-id",
  "url": "http://...",
  "data": [
    {"name": "first", "value": "您好", "color": "#173177"},
    {"name": "keyword1", "value": "测试"}
  ]
}
```

#### 发送文本消息
```
POST /api/wechat/message/text?openId=xxx&content=Hello
```

#### 发送图文消息
```
POST /api/wechat/message/news
Content-Type: application/json

{
  "openId": "openid",
  "title": "标题",
  "description": "描述",
  "url": "http://...",
  "picUrl": "http://..."
}
```

### 多账号管理 API 🆕

#### 获取账号列表
```
GET /api/wechat/admin/accounts?accountType=mp
```

#### 创建账号
```
POST /api/wechat/admin/accounts
Content-Type: application/json

{
  "accountType": "mp",
  "accountName": "官方公众号",
  "appId": "wx123456",
  "appSecret": "secret",
  "isDefault": true,
  "isEnabled": true
}
```

#### 更新账号
```
PUT /api/wechat/admin/accounts/{id}
```

#### 删除账号
```
DELETE /api/wechat/admin/accounts/{id}
```

#### 设置默认账号
```
POST /api/wechat/admin/accounts/{id}/set-default
```

#### 启用/禁用账号
```
POST /api/wechat/admin/accounts/{id}/toggle?enabled=true
```

#### 获取账号配置
```
GET /api/wechat/admin/accounts/{accountId}/configs
```

#### 保存配置
```
POST /api/wechat/admin/accounts/{accountId}/configs
Content-Type: application/json

{
  "configKey": "token",
  "configValue": "your-token",
  "isEncrypted": false
}
```

#### 刷新配置缓存
```
POST /api/wechat/admin/refresh-cache
```

## 多账号使用示例

### 使用指定账号发送消息

```java
@Service
@RequiredArgsConstructor
public class MultiAccountService {
    
    private final Map<Long, WxMpService> wxMpServiceMap;
    
    // 从指定账号发送消息
    public void sendFromAccount(Long accountId, String openId, String content) {
        WxMpService service = wxMpServiceMap.get(accountId);
        if (service != null) {
            WxMpKefuMessage message = WxMpKefuMessage.TEXT()
                    .toUser(openId)
                    .content(content)
                    .build();
            service.getKefuService().sendKefuMessage(message);
        }
    }
}
```

### 根据 AppID 路由账号

```java
@Service
@RequiredArgsConstructor
public class WeChatCallbackHandler {
    
    private final WeChatMultiAccountConfigService configService;
    private final Map<Long, WxMpService> wxMpServiceMap;
    
    // 处理微信回调（根据 AppID 路由）
    public void handleCallback(String appId, String signature, String timestamp, 
                               String nonce, String echostr) {
        // 根据 AppID 加载配置
        WeChatMpProperties properties = configService.loadMpConfigByAppId(appId);
        if (properties != null) {
            // 找到对应的服务实例并处理
            // ...
        }
    }
}
```

## 集成到现有认证体系

### 1. 扩展 OAuth2ProviderStrategy

如果要与现有的 OAuth2 登录体系集成，可以创建一个适配器：

```java
@Component
public class WeChatOAuth2Adapter implements OAuth2ProviderStrategy {
    
    private final WeChatLoginService weChatLoginService;
    
    @Override
    public String getProviderType() {
        return "wechat-mp";
    }
    
    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        // 适配微信用户信息到标准格式
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        return StandardOAuth2UserInfo.builder()
                .provider("wechat-mp")
                .openId((String) attributes.get("openid"))
                .unionId((String) attributes.get("unionid"))
                .nickname((String) attributes.get("nickname"))
                .avatar((String) attributes.get("headimgurl"))
                .build();
    }
}
```

### 2. 集成到 Notification 模块

在 `mortise-notification` 模块中创建 SPI 实现：

```java
@Service
@RequiredArgsConstructor
@ConditionalOnBean(WeChatMessageService.class)
public class WeChatNotificationSender implements NotificationSender {
    
    private final WeChatMessageService weChatMessageService;
    
    @Override
    public String getType() {
        return "WECHAT_TEMPLATE";
    }
    
    @Override
    public void send(NotificationMessage message) {
        // 发送微信模板消息
    }
}
```

## 文档索引

- [快速开始指南](docs/WECHAT_QUICK_START.md)
- [部署指南](docs/WECHAT_DEPLOYMENT_GUIDE.md)
- [数据库配置说明](docs/WECHAT_DATABASE_CONFIG.md)
- **[多账号管理指南](docs/WECHAT_MULTI_ACCOUNT_GUIDE.md)** 🆕
- [实现总结](docs/WECHAT_IMPLEMENTATION_SUMMARY.md)

## 注意事项

1. **安全性**
   - 生产环境必须使用 HTTPS
   - State 参数必须验证，防止 CSRF 攻击
   - 不要在前端暴露 AppSecret
   - 敏感配置（AppSecret、AESKey）使用加密存储
   - 账号管理接口添加权限控制

2. **微信限制**
   - 模板消息有发送频率限制
   - 客服消息只能在 48 小时内发送
   - 二维码有效期默认 5 分钟

3. **OpenID vs UnionID**
   - OpenID：同一用户在不同公众号下不同
   - UnionID：同一用户在同一开放平台下相同
   - 建议使用 UnionID 作为用户唯一标识

4. **测试账号**
   - 微信提供测试公众号：https://mp.weixin.qq.com/debug/cgi-bin/sandbox
   - 测试账号有功能限制，正式上线需申请正式账号

5. **多账号注意事项** 🆕
   - 每种账号类型只能有一个默认账号
   - 修改配置后需刷新缓存
   - 建议使用 REST API 管理账号，避免直接操作数据库
   - AppID 在系统内应唯一，避免重复配置

## 技术支持

- WxJava 官方文档：https://github.com/binarywang/WxJava
- 微信公众平台：https://mp.weixin.qq.com
- 微信开放平台：https://open.weixin.qq.com

## 开发计划

- [x] 微信公众号消息推送
- [x] 微信扫码登录（PC + H5）
- [x] 数据库动态配置加载
- [x] 多账号管理支持 🆕
- [ ] 支持微信小程序登录
- [ ] 支持企业微信
- [ ] 消息推送队列化
- [ ] 用户标签管理
- [ ] 素材管理

## 更新日志

### v1.1.0 (2024-01-XX) 🆕

- ✅ 支持多微信账号管理
- ✅ 数据库表重构（account + config 分离）
- ✅ 新增账号管理服务和 API
- ✅ 配置缓存优化（支持账号级缓存）
- ✅ 敏感信息加密存储
- ✅ 完善多账号文档

### v1.0.0 (2024-01-XX)

- ✅ 微信公众号消息推送
- ✅ PC 扫码登录
- ✅ H5 授权登录
- ✅ 数据库配置加载

