# WeChatAccessTokenResponseClient 快速参考

## 🎯 一句话说明
专门处理微信 OAuth2 token 获取的客户端，解决微信 API 的特殊性问题。

## 🔧 主要功能

### 1. 支持微信特殊的响应格式
- ✅ 处理 `text/plain` Content-Type（而非标准的 `application/json`）
- ✅ 自动补充缺失的 `token_type` 字段
- ✅ 识别并处理微信错误码（errcode/errmsg）

### 2. 完整的错误处理
```java
// 微信 API 错误
if (response.errcode) → OAuth2AuthenticationException("wechat_error")

// 网络错误
RestClientException → OAuth2AuthenticationException("invalid_token_response")

// 其他错误
Exception → OAuth2AuthenticationException("server_error")
```

### 3. 详细的日志记录
```
INFO  - 初始化成功
DEBUG - 开始获取 token
DEBUG - 补充 token_type 字段
INFO  - 成功获取 token
ERROR - 错误信息（含 errcode）
```

## 📝 使用方式

### 自动配置（推荐）
该类使用 `@Component` 注解，Spring 会自动注入，无需手动配置。

### 手动配置 OAuth2（如需要）
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            WeChatAccessTokenResponseClient tokenClient) throws Exception {
        
        http.oauth2Login(oauth2 -> oauth2
            .tokenEndpoint(token -> token
                .accessTokenResponseClient(tokenClient)  // 使用微信专用客户端
            )
        );
        
        return http.build();
    }
}
```

## 🔍 技术细节

### 请求流程

```
1. buildTokenRequest()
   ↓ 构建请求参数
   ├─ grant_type: authorization_code
   ├─ code: 授权码
   ├─ redirect_uri: 回调地址
   ├─ client_id: 应用ID
   └─ client_secret: 应用密钥

2. restOperations.exchange()
   ↓ 发送 HTTP POST 请求

3. 检查响应
   ├─ 是否包含 errcode? → 抛出异常
   ├─ 是否缺少 token_type? → 自动补充
   └─ 转换为标准 OAuth2AccessTokenResponse

4. 返回结果
```

### 支持的媒体类型

| Content-Type | 用途 |
|--------------|------|
| `application/json` | 标准 JSON 响应 |
| `text/plain` | 微信 API 响应（重点） |
| `application/*+json` | JSON 变体 |

### 微信错误码示例

| errcode | errmsg | 说明 |
|---------|--------|------|
| 40001 | invalid credential | access_token 无效 |
| 40013 | invalid appid | AppID 无效 |
| 40029 | invalid code | 授权码无效 |
| 40163 | code been used | 授权码已使用 |

## 🐛 常见问题

### Q1: 为什么需要单独的客户端？
**A**: 微信 OAuth2 API 有两个特殊性：
1. 返回 Content-Type 是 `text/plain` 而非 `application/json`
2. 不返回标准的 `token_type` 字段

Spring Security 默认客户端无法处理这些情况。

### Q2: 日志级别如何配置？
**A**: 在 `application.yml` 中：
```yaml
logging:
  level:
    com.rymcu.mortise.auth.support.WeChatAccessTokenResponseClient: DEBUG
```

### Q3: 如何测试？
**A**: 
```java
@SpringBootTest
class WeChatTokenClientTest {
    
    @Autowired
    private WeChatAccessTokenResponseClient client;
    
    @Test
    void testTokenResponse() {
        // 创建模拟请求
        OAuth2AuthorizationCodeGrantRequest request = ...;
        
        // 获取 token
        OAuth2AccessTokenResponse response = client.getTokenResponse(request);
        
        // 验证结果
        assertNotNull(response.getAccessToken());
    }
}
```

### Q4: 与旧版本的区别？
**A**: 
| 项目 | 旧版本 | 新版本 |
|------|--------|--------|
| 弃用 API | ✅ 使用 | ❌ 已移除 |
| 错误处理 | 基础 | 完善（含微信特定错误） |
| 日志记录 | 无 | 完整的调试信息 |
| 代码结构 | 单一方法 | 模块化 |
| 文档 | 简单注释 | 详细 JavaDoc |

## 🎓 扩展学习

### 相关类和接口

```
OAuth2AccessTokenResponseClient          ← 接口
    ↑
WeChatAccessTokenResponseClient         ← 实现（本类）

相关类：
- OAuth2AuthorizationCodeGrantRequest   ← 请求参数
- OAuth2AccessTokenResponse             ← 响应结果
- DefaultMapOAuth2AccessTokenResponseConverter ← 转换器
```

### 核心依赖

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
</dependency>
```

## 📋 检查清单

在使用此类之前，确保：

- [ ] 已配置微信 OAuth2 客户端注册信息
- [ ] `client_authentication_method` 设置为 `client_secret_post`
- [ ] Token URI 设置为微信的 token 端点
- [ ] 已启用 OAuth2 Login
- [ ] 日志级别配置正确（可选）

## 🔗 相关文档

- [优化总结](./wechat-token-client-optimization.md) - 详细的优化说明
- [OAuth2 配置指南](./oauth2-configuration-guide.md) - OAuth2 完整配置
- [动态 OAuth2 客户端](./DYNAMIC_OAUTH2_IMPLEMENTATION_SUMMARY.md) - 动态客户端实现

---

**最后更新**: 2025-10-07  
**维护者**: ronger  
**版本**: 1.0.0
