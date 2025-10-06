# 微信集成快速入门

## 🎯 5 分钟快速集成

### 1. 添加依赖

在 `mortise-app/pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-wechat</artifactId>
</dependency>
```

### 2. 配置参数

在 `application-dev.yml` 中添加：

```yaml
wechat:
  mp:
    enabled: true
    app-id: wx_your_appid        # 公众号 AppID
    app-secret: your_secret       # 公众号 AppSecret
    token: your_token             # 自定义 Token
  
  open:
    enabled: true
    app-id: wx_open_appid         # 开放平台 AppID
    app-secret: open_secret       # 开放平台 AppSecret
    redirect-uri: http://localhost:8080/api/wechat/login/callback
```

### 3. 启动测试

```bash
mvn clean install
cd mortise-app
mvn spring-boot:run
```

### 4. 测试接口

```bash
# 获取扫码登录 URL
curl http://localhost:8080/api/wechat/login/qrcode-url?redirectUri=http://localhost:8080/callback

# 发送测试消息
curl -X POST http://localhost:8080/api/wechat/message/text \
  -d "openId=your_openid&content=Hello"
```

## 📖 完整文档

详细部署和集成指南，请参阅：
- [完整部署指南](./WECHAT_DEPLOYMENT_GUIDE.md)
- [模块 README](../mortise-wechat/README.md)

## 🔑 获取测试账号

开发测试可使用微信测试公众号：
👉 https://mp.weixin.qq.com/debug/cgi-bin/sandbox

立即获得：
- ✅ 免费的 AppID 和 AppSecret
- ✅ 完整的 API 调用权限
- ✅ 无需企业认证

## 💡 使用示例

### 扫码登录

```java
@RestController
@RequiredArgsConstructor
public class AuthController {
    
    private final WeChatLoginService weChatLoginService;
    
    @GetMapping("/wechat/login")
    public Map<String, String> getLoginUrl() {
        String url = weChatLoginService.buildAuthorizationUrl(
            "http://your-domain.com/callback",
            UUID.randomUUID().toString()
        );
        return Map.of("qrcodeUrl", url);
    }
}
```

### 消息推送

```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final WeChatMessageService messageService;
    
    public void sendWelcome(String openId) throws WxErrorException {
        TemplateMessage msg = TemplateMessage.builder()
            .toUser(openId)
            .templateId("template_id")
            .build()
            .addData("first", "欢迎使用！")
            .addData("keyword1", "新用户")
            .addData("remark", "感谢注册");
        
        messageService.sendTemplateMessage(msg);
    }
}
```

## 🚀 下一步

- [ ] 配置生产环境 HTTPS
- [ ] 设置微信回调域名
- [ ] 集成到用户认证系统
- [ ] 实现业务通知推送

有问题？查看 [常见问题](./WECHAT_DEPLOYMENT_GUIDE.md#常见问题) 或提交 Issue。
