# 微信专用组件集成指南

## 概述

本指南介绍如何将微信 OAuth2 专用组件集成到 Mortise 的动态 OAuth2 配置流程中。采用类似 `UnifiedOAuth2UserService` 的设计理念，实现非侵入性的集成。

## 设计理念

### 非侵入性设计

与直接修改 `WebSecurityConfig` 不同，我们采用了统一组件的设计模式：

```java
// 类似 UnifiedOAuth2UserService 的设计
UnifiedOAuth2AccessTokenResponseClient     // 统一的 Token 客户端
UnifiedOAuth2AuthorizationRequestResolver  // 统一的授权请求解析器
```

这种设计的优势：
- **非侵入性**：不需要修改现有的 `WebSecurityConfig`
- **扩展性**：可以轻松为其他社交平台添加专用处理逻辑
- **向后兼容**：对于不需要特殊处理的提供商，使用标准逻辑

## 组件架构

### 1. UnifiedOAuth2AccessTokenResponseClient

```java
@Component
public class UnifiedOAuth2AccessTokenResponseClient {
    
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        
        if (isWeChatProvider(registrationId)) {
            // 使用微信专用处理逻辑
            return weChatClient.getTokenResponse(request);
        }
        
        // 其他提供商使用标准处理逻辑
        return getStandardTokenResponse(request);
    }
}
```

### 2. UnifiedOAuth2AuthorizationRequestResolver

```java
@Component  
public class UnifiedOAuth2AuthorizationRequestResolver {
    
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest request) {
        String registrationId = request.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        
        if (isWeChatProvider(registrationId)) {
            // 为微信添加 #wechat_redirect 锚点
            return customizeForWeChat(request);
        }
        
        // 其他提供商不做修改
        return request;
    }
}
```

## 集成方式

### 1. 自动集成

统一组件会通过 `@Component` 自动注册，`WebSecurityConfig` 会自动检测并使用：

```java
// WebSecurityConfig.java 中的自动集成
private final ObjectProvider<UnifiedOAuth2AccessTokenResponseClient> unifiedAccessTokenResponseClientProvider;
private final ObjectProvider<UnifiedOAuth2AuthorizationRequestResolver> unifiedAuthorizationRequestResolverProvider;

private void configureOAuth2Login(HttpSecurity http, ClientRegistrationRepository repository) {
    http.oauth2Login(oauth2Login -> {
        oauth2Login
            .authorizationEndpoint(authorization -> {
                // 自动使用统一授权请求解析器（如果存在）
                authorization.authorizationRequestResolver(
                    authorizationRequestResolver(repository)
                );
            })
            .tokenEndpoint(token -> {
                // 自动使用统一 Token 客户端（如果存在）
                unifiedAccessTokenResponseClientProvider.ifAvailable(
                    token::accessTokenResponseClient
                );
            });
    });
}
```

### 2. 降级机制

如果统一组件不存在，系统会自动降级到标准组件：

```java
// 授权请求解析器的降级逻辑
private OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository repository) {
    
    // 优先使用统一解析器
    UnifiedOAuth2AuthorizationRequestResolver unifiedResolver = 
            unifiedAuthorizationRequestResolverProvider.getIfAvailable();
    
    if (unifiedResolver != null) {
        log.info("使用统一 OAuth2 授权请求解析器（支持微信等特殊处理）");
        return unifiedResolver;
    }
    
    // 降级到默认解析器
    log.info("使用默认 OAuth2 授权请求解析器");
    return new DefaultOAuth2AuthorizationRequestResolver(repository, "/oauth2/authorization");
}
```

## 使用配置

### 1. 启用微信组件

组件默认已经通过 `@Component` 注解自动注册。如果需要条件化启用，可以添加配置：

```yaml
# application.yml
mortise:
  oauth2:
    dynamic-client-enabled: true  # 启用动态 OAuth2
    wechat:
      custom-components-enabled: true  # 启用微信专用组件（可选配置项）
```

### 2. 数据库配置微信客户端

在数据库中添加微信客户端配置：

```sql
INSERT INTO mortise_oauth2_client_config (
    registration_id,
    client_id,
    client_secret,
    client_name,
    scopes,
    redirect_uri_template,
    client_authentication_method,
    authorization_grant_type,
    authorization_uri,
    token_uri,
    user_info_uri,
    user_name_attribute,
    enabled
) VALUES (
    'wechat_official',
    'your-wechat-appid',
    'your-wechat-secret',
    '微信官方账号登录',
    'snsapi_login',
    '{baseUrl}/login/oauth2/code/{registrationId}',
    'client_secret_post',
    'authorization_code',
    'https://open.weixin.qq.com/connect/qrconnect',
    'https://api.weixin.qq.com/sns/oauth2/access_token',
    'https://api.weixin.qq.com/sns/userinfo',
    'openid',
    TRUE
);
```

### 3. 前端登录链接

```html
<!-- 微信登录链接会自动获得 #wechat_redirect 锚点 -->
<a href="/oauth2/authorization/wechat_official">使用微信登录</a>
```

## 工作流程

### 1. 用户点击微信登录

```
用户点击登录链接: /oauth2/authorization/wechat_official
    ↓
Spring Security 拦截请求
    ↓
调用 WeChatAuthorizationRequestResolver.resolve()
    ↓
构建授权请求并添加 #wechat_redirect 锚点
    ↓
重定向到微信授权页面: https://open.weixin.qq.com/connect/qrconnect?...#wechat_redirect
```

### 2. 微信回调处理

```
微信授权成功回调: /login/oauth2/code/wechat_official?code=xxx
    ↓
Spring Security 处理回调
    ↓
调用 WeChatAccessTokenResponseClient.getTokenResponse()
    ↓
发送 Token 请求到微信 API
    ↓
处理微信的 text/plain 响应格式
    ↓
补充缺失的 token_type 字段
    ↓
转换为标准的 OAuth2AccessTokenResponse
    ↓
继续标准的 OAuth2 登录流程
```

## 特殊处理说明

### 1. 微信 API 兼容性处理

`WeChatAccessTokenResponseClient` 解决了以下微信 API 的特殊问题：

- **Content-Type 问题**: 微信返回 `text/plain` 而不是标准的 `application/json`
- **缺失字段**: 微信不返回 `token_type` 字段，需要手动补充为 `Bearer`
- **错误处理**: 处理微信特有的 `errcode`/`errmsg` 错误格式

### 2. 锚点处理

`WeChatAuthorizationRequestResolver` 为微信授权 URL 添加 `#wechat_redirect` 锚点，这是微信网页授权的要求。

## 多微信应用支持

可以配置多个微信应用：

```sql
-- 微信应用 A
INSERT INTO mortise_oauth2_client_config (...) VALUES (
    'wechat_app_a', 'appid_a', 'secret_a', '微信应用A', ...
);

-- 微信应用 B  
INSERT INTO mortise_oauth2_client_config (...) VALUES (
    'wechat_app_b', 'appid_b', 'secret_b', '微信应用B', ...
);
```

前端提供不同的登录入口：

```html
<a href="/oauth2/authorization/wechat_app_a">应用A微信登录</a>
<a href="/oauth2/authorization/wechat_app_b">应用B微信登录</a>
```

## 错误处理

### 1. 组件不存在时的降级

如果微信专用组件不存在（例如在某些环境中禁用），系统会自动降级到标准组件：

- `WeChatAuthorizationRequestResolver` 不存在 → 使用 `DefaultOAuth2AuthorizationRequestResolver`
- `WeChatAccessTokenResponseClient` 不存在 → 使用默认的 Token 客户端

### 2. 微信 API 错误处理

`WeChatAccessTokenResponseClient` 提供详细的错误处理：

```java
// 检查微信错误码
if (responseMap.containsKey("errcode")) {
    int errCode = ((Number) responseMap.get("errcode")).intValue();
    String errMsg = (String) responseMap.getOrDefault("errmsg", "Unknown error");
    throw new OAuth2AuthenticationException(
        new OAuth2Error("wechat_error", 
            String.format("WeChat API error: %d - %s", errCode, errMsg), null)
    );
}
```

## 监控和日志

### 1. 关键日志点

```java
// 组件选择日志
log.info("使用微信专用授权请求解析器");
log.info("使用默认授权请求解析器");

// Token 获取日志
log.info("成功获取客户端 [{}] 的访问令牌", registrationId);
log.error("微信 Token 请求失败 - errcode: {}, errmsg: {}", errCode, errMsg);
```

### 2. 性能监控

可以通过 Spring Boot Actuator 监控 OAuth2 登录的成功率和响应时间。

## 故障排查

### 1. 常见问题

**问题**: 微信登录后出现 "invalid_token_response" 错误
**解决**: 检查 `WeChatAccessTokenResponseClient` 是否正确配置和注册

**问题**: 微信授权页面显示异常
**解决**: 检查授权 URL 是否包含 `#wechat_redirect` 锚点

**问题**: 多个微信应用配置冲突
**解决**: 确保每个微信应用使用不同的 `registration_id`

### 2. 调试技巧

启用 OAuth2 调试日志：

```yaml
logging:
  level:
    org.springframework.security.oauth2: DEBUG
    com.rymcu.mortise.auth.support: DEBUG
```

## 扩展建议

### 1. 条件化配置

可以添加配置属性来控制微信组件的启用：

```java
@Component
@ConditionalOnProperty(
    prefix = "mortise.oauth2.wechat",
    name = "custom-components-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class WeChatAccessTokenResponseClient { ... }
```

### 2. 其他社交平台支持

可以参考微信组件的实现，为其他有特殊要求的社交平台（如钉钉、企业微信等）创建类似的专用组件。