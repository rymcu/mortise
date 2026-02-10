# 微信集成模块部署指南

本文档提供 `mortise-wechat` 模块的完整部署和集成指南。

## 📋 目录

1. [前置准备](#前置准备)
2. [模块集成](#模块集成)
3. [配置微信参数](#配置微信参数)
4. [功能验证](#功能验证)
5. [生产部署](#生产部署)
6. [常见问题](#常见问题)

---

## 🔧 前置准备

### 1. 申请微信公众号

1. 访问 [微信公众平台](https://mp.weixin.qq.com/)
2. 注册并认证公众号（服务号或订阅号）
3. 获取以下信息：
   - AppID
   - AppSecret
   - Token（自定义）
   - EncodingAESKey（可选，用于消息加解密）

**开发阶段可以使用测试公众号：**
- 访问：https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login
- 免费获得测试 AppID 和 AppSecret
- 功能受限，但适合开发测试

### 2. 申请微信开放平台（扫码登录）

1. 访问 [微信开放平台](https://open.weixin.qq.com/)
2. 注册开发者账号
3. 创建「网站应用」
4. 填写网站信息和授权回调域名
5. 获取：
   - AppID
   - AppSecret

**注意：**
- 网站应用需要审核（1-3 个工作日）
- 回调域名必须是已备案的域名
- 开发环境可以配置 `localhost` 或内网穿透域名

### 3. 准备开发环境

确保已安装：
- JDK 21
- Maven 3.8+
- Redis（用于缓存）

---

## 🚀 模块集成

### 步骤 1: 在 mortise-app 中添加依赖

编辑 `mortise-app/pom.xml`，添加：

```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-wechat</artifactId>
</dependency>
```

### 步骤 2: 配置 Spring Boot 扫描

在 `mortise-app` 的主类中确保包扫描包含微信模块：

```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.rymcu.mortise",
    "com.rymcu.mortise.wechat"  // 添加这行
})
public class MortiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(MortiseApplication.class, args);
    }
}
```

### 步骤 3: 引入配置文件

在 `mortise-app/src/main/resources/application.yml` 中引入微信配置：

```yaml
spring:
  profiles:
    include:
      - wechat  # 添加这行
```

---

## ⚙️ 配置微信参数

### 开发环境配置

编辑 `mortise-app/src/main/resources/application-dev.yml`：

```yaml
# 微信公众号配置
wechat:
  mp:
    enabled: true
    app-id: wx1234567890abcdef  # 替换为你的公众号 AppID
    app-secret: your-mp-secret   # 替换为你的公众号 AppSecret
    token: mortise_token_2024    # 自定义 Token
    aes-key: your-aes-key        # 消息加解密密钥（可选）

  # 微信开放平台配置（扫码登录）
  open:
    enabled: true
    app-id: wxabcdef1234567890  # 替换为开放平台 AppID
    app-secret: your-open-secret # 替换为开放平台 AppSecret
    redirect-uri: http://localhost:8080/api/wechat/login/callback
    qr-code-expire-seconds: 300
```

### 生产环境配置

**强烈建议使用环境变量，不要在代码中硬编码密钥！**

编辑 `mortise-app/src/main/resources/application-prod.yml`：

```yaml
wechat:
  mp:
    enabled: true
    app-id: ${WECHAT_MP_APP_ID}
    app-secret: ${WECHAT_MP_APP_SECRET}
    token: ${WECHAT_MP_TOKEN}
    aes-key: ${WECHAT_MP_AES_KEY}

  open:
    enabled: true
    app-id: ${WECHAT_OPEN_APP_ID}
    app-secret: ${WECHAT_OPEN_APP_SECRET}
    redirect-uri: ${WECHAT_OPEN_REDIRECT_URI}
```

### Docker 环境变量配置

在 `docker-compose.yml` 或 `.env` 文件中：

```env
WECHAT_MP_APP_ID=wx1234567890abcdef
WECHAT_MP_APP_SECRET=your-secret
WECHAT_MP_TOKEN=mortise_token
WECHAT_MP_AES_KEY=your-aes-key

WECHAT_OPEN_APP_ID=wxabcdef1234567890
WECHAT_OPEN_APP_SECRET=your-open-secret
WECHAT_OPEN_REDIRECT_URI=https://your-domain.com/api/wechat/login/callback
```

---

## ✅ 功能验证

### 1. 启动应用

```bash
cd mortise-app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

查看日志，确认微信服务初始化成功：

```
微信公众号服务初始化成功，AppID: wx1234567890abcdef
微信开放平台服务初始化成功，AppID: wxabcdef1234567890
```

### 2. 测试扫码登录接口

#### 获取二维码 URL

```bash
curl "http://localhost:8080/api/wechat/login/qrcode-url?redirectUri=http://localhost:8080/api/wechat/login/callback"
```

响应示例：
```json
{
  "authUrl": "https://open.weixin.qq.com/connect/qrconnect?appid=...",
  "state": "uuid-string"
}
```

#### 前端展示二维码

```html
<div id="qrcode"></div>

<script>
fetch('/api/wechat/login/qrcode-url?redirectUri=' + encodeURIComponent(window.location.origin + '/callback'))
  .then(res => res.json())
  .then(data => {
    // 使用 qrcode.js 生成二维码
    new QRCode(document.getElementById('qrcode'), {
      text: data.authUrl,
      width: 256,
      height: 256
    });
    
    // 保存 state 用于验证
    sessionStorage.setItem('wechat_state', data.state);
  });
</script>
```

### 3. 测试消息推送

```bash
curl -X POST http://localhost:8080/api/wechat/message/text \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "openId=o6_bmjrPTlm6_2sgVt7hMZOPfL2M&content=测试消息"
```

---

## 🌐 生产部署

### 1. 配置 HTTPS

微信要求生产环境必须使用 HTTPS：

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location /api/wechat/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 2. 配置微信回调域名

在微信公众平台和开放平台中配置：

**公众号：**
- 设置与开发 -> 公众号设置 -> 功能设置
- 配置「网页授权域名」：`your-domain.com`

**开放平台：**
- 网站应用详情 -> 开发信息
- 配置「授权回调域」：`your-domain.com`

### 3. 配置服务器 IP 白名单

在微信公众平台：
- 基本配置 -> IP 白名单
- 添加服务器公网 IP

### 4. 环境变量管理

使用 Kubernetes Secrets 或 Docker Secrets：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: wechat-secrets
type: Opaque
stringData:
  WECHAT_MP_APP_ID: "wx1234567890abcdef"
  WECHAT_MP_APP_SECRET: "your-secret"
  WECHAT_OPEN_APP_ID: "wxabcdef1234567890"
  WECHAT_OPEN_APP_SECRET: "your-open-secret"
```

### 5. 监控和日志

启用微信 API 调用监控：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,wechat
  
logging:
  level:
    me.chanjar.weixin: DEBUG  # 开发环境
    # me.chanjar.weixin: WARN  # 生产环境
```

---

## 🔗 与现有模块集成

### 集成到认证系统

创建 `WeChatAuthenticationProvider`：

```java
@Component
public class WeChatAuthenticationProvider {
    
    private final WeChatLoginService weChatLoginService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    
    public String handleWeChatLogin(String code) throws WxErrorException {
        // 1. 获取微信用户信息
        WxOAuth2UserInfo wxUser = weChatLoginService.getUserInfoByCode(code);
        
        // 2. 查找或创建用户
        User user = userService.findByUnionId(wxUser.getUnionId());
        if (user == null) {
            user = userService.createFromWechat(wxUser);
        }
        
        // 3. 生成 JWT Token
        return jwtTokenUtil.generateToken(user);
    }
}
```

### 集成到通知系统

实现 `NotificationSender` SPI：

```java
@Service
@ConditionalOnBean(WeChatMessageService.class)
public class WeChatNotificationSender implements NotificationSender {
    
    private final WeChatMessageService weChatMessageService;
    
    @Override
    public String getType() {
        return "WECHAT_MP";
    }
    
    @Override
    public void send(NotificationRequest request) {
        try {
            weChatMessageService.sendTextMessage(
                request.getRecipient(),
                request.getContent()
            );
        } catch (WxErrorException e) {
            throw new NotificationException("微信消息发送失败", e);
        }
    }
}
```

---

## ❓ 常见问题

### Q1: 为什么收不到消息？

**A:** 检查以下几点：
1. 用户是否关注了公众号（客服消息需要用户在 48 小时内互动）
2. 模板消息是否已在公众平台添加
3. 检查日志中的微信 API 错误码

### Q2: 扫码登录一直转圈？

**A:** 可能原因：
1. 回调地址配置错误
2. State 参数验证失败
3. 前端未正确处理回调

### Q3: 生产环境报 40164 错误？

**A:** IP 白名单未配置，在公众平台添加服务器 IP。

### Q4: Access Token 过期怎么办？

**A:** WxJava 会自动刷新 Token，无需手动处理。建议启用 Redis 缓存：

```yaml
spring:
  cache:
    type: redis
```

### Q5: 如何测试微信回调？

**A:** 使用内网穿透工具：
- [ngrok](https://ngrok.com/)
- [frp](https://github.com/fatedier/frp)
- [natapp](https://natapp.cn/)

```bash
ngrok http 8080
# 获得临时域名: https://abc123.ngrok.io
```

---

## 📚 参考资料

- [WxJava 官方文档](https://github.com/binarywang/WxJava/wiki)
- [微信公众平台技术文档](https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html)
- [微信开放平台文档](https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html)
- [微信公众平台测试账号](https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login)

---

## 📞 技术支持

如遇到问题，请提供：
1. 完整的错误日志
2. 微信 API 返回的错误码
3. 配置文件（隐藏敏感信息）
4. 操作步骤

祝你部署顺利！🎉
