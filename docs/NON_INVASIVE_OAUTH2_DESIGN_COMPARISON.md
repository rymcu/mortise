# 非侵入性 OAuth2 扩展设计对比

## 设计对比

### 破坏性重构方式（不推荐）

```java
// 直接修改 WebSecurityConfig
@Configuration
public class WebSecurityConfig {
    
    // 直接依赖微信专用组件
    private final WeChatAccessTokenResponseClient weChatClient;
    private final WeChatAuthorizationRequestResolver weChatResolver;
    
    private void configureOAuth2Login(HttpSecurity http) {
        http.oauth2Login(oauth2 -> {
            oauth2
                .authorizationEndpoint(auth -> 
                    auth.authorizationRequestResolver(weChatResolver))  // 硬编码微信组件
                .tokenEndpoint(token -> 
                    token.accessTokenResponseClient(weChatClient));     // 硬编码微信组件
        });
    }
}
```

**问题**：
- 破坏了现有架构
- 只支持微信，扩展困难
- 与动态配置流程不兼容

### 统一组件方式（推荐）

```java
// 统一组件设计
@Component
public class UnifiedOAuth2AccessTokenResponseClient {
    
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        
        // 智能分发
        if (isWeChatProvider(registrationId)) {
            return handleWeChatRequest(request);        // 微信专用逻辑
        } else if (isDingTalkProvider(registrationId)) {
            return handleDingTalkRequest(request);      // 钉钉专用逻辑（未来扩展）
        }
        
        return handleStandardRequest(request);          // 标准 OAuth2 逻辑
    }
}

// WebSecurityConfig 保持不变
@Configuration
public class WebSecurityConfig {
    
    // 使用 ObjectProvider 保持可选性
    private final ObjectProvider<UnifiedOAuth2AccessTokenResponseClient> unifiedClientProvider;
    
    private void configureOAuth2Login(HttpSecurity http) {
        http.oauth2Login(oauth2 -> {
            oauth2.tokenEndpoint(token -> {
                // 自动检测并使用统一组件
                unifiedClientProvider.ifAvailable(token::accessTokenResponseClient);
            });
        });
    }
}
```

**优势**：
- 非侵入性，不破坏现有架构
- 易于扩展，支持多种社交平台
- 完全兼容动态配置流程

## 扩展示例

### 添加钉钉支持

```java
// 在统一组件中添加钉钉支持
@Component
public class UnifiedOAuth2AccessTokenResponseClient {
    
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        
        if (isWeChatProvider(registrationId)) {
            return handleWeChatRequest(request);
        } else if (isDingTalkProvider(registrationId)) {
            // 新增钉钉支持，无需修改 WebSecurityConfig
            return handleDingTalkRequest(request);
        } else if (isEnterpriseWeChatProvider(registrationId)) {
            // 新增企业微信支持，无需修改 WebSecurityConfig
            return handleEnterpriseWeChatRequest(request);
        }
        
        return handleStandardRequest(request);
    }
    
    private boolean isDingTalkProvider(String registrationId) {
        return registrationId != null && 
               registrationId.toLowerCase().contains("dingtalk");
    }
    
    private OAuth2AccessTokenResponse handleDingTalkRequest(OAuth2AuthorizationCodeGrantRequest request) {
        // 钉钉特有的 Token 获取逻辑
        // 例如：处理钉钉的 tmp_auth_code
        // ...
    }
}
```

### 添加授权请求自定义

```java
// 在统一授权解析器中添加多平台支持
@Component
public class UnifiedOAuth2AuthorizationRequestResolver {
    
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest request) {
        String registrationId = request.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        
        if (isWeChatProvider(registrationId)) {
            return customizeForWeChat(request);         // 添加 #wechat_redirect
        } else if (isDingTalkProvider(registrationId)) {
            return customizeForDingTalk(request);       // 添加钉钉特有参数
        } else if (isGoogleProvider(registrationId)) {
            return customizeForGoogle(request);         // 添加 prompt=consent
        }
        
        return request;  // 其他平台不做修改
    }
    
    private OAuth2AuthorizationRequest customizeForDingTalk(OAuth2AuthorizationRequest request) {
        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(params -> {
                    params.put("response_type", "code");
                    params.put("prompt", "consent");    // 钉钉特有参数
                })
                .build();
    }
}
```

## 使用效果

### 前端代码

```html
<!-- 支持多种社交平台，无需修改后端配置 -->
<a href="/oauth2/authorization/wechat_official">微信登录</a>
<a href="/oauth2/authorization/wechat_corp">企业微信登录</a>
<a href="/oauth2/authorization/dingtalk">钉钉登录</a>
<a href="/oauth2/authorization/github">GitHub登录</a>
<a href="/oauth2/authorization/google">Google登录</a>
```

### 数据库配置

```sql
-- 微信官方账号
INSERT INTO mortise_oauth2_client_config (...) VALUES (
    'wechat_official', 'wx_appid_1', 'wx_secret_1', '微信官方账号', ...
);

-- 企业微信
INSERT INTO mortise_oauth2_client_config (...) VALUES (
    'wechat_corp', 'wx_corp_id', 'wx_corp_secret', '企业微信', ...
);

-- 钉钉
INSERT INTO mortise_oauth2_client_config (...) VALUES (
    'dingtalk', 'dingtalk_appid', 'dingtalk_secret', '钉钉', ...
);
```

### 自动处理流程

```
用户点击登录链接
    ↓
Spring Security 拦截
    ↓
UnifiedOAuth2AuthorizationRequestResolver.resolve()
    ↓ 
根据 registrationId 自动选择处理策略:
    - wechat_* → 添加 #wechat_redirect 锚点
    - dingtalk → 添加钉钉特有参数  
    - google → 添加 prompt=consent
    - 其他 → 标准处理
    ↓
重定向到对应平台授权页面
    ↓
授权回调
    ↓
UnifiedOAuth2AccessTokenResponseClient.getTokenResponse()
    ↓
根据 registrationId 自动选择处理策略:
    - wechat_* → 处理 text/plain 响应，补充 token_type
    - dingtalk → 处理钉钉特有的 Token 格式
    - 其他 → 标准 OAuth2 处理
    ↓
继续标准登录流程
```

## 总结

### 统一组件设计的优势

1. **非侵入性**：不需要修改 `WebSecurityConfig`，保持架构整洁
2. **扩展性**：可以轻松添加新的社交平台支持
3. **可维护性**：每个平台的特殊逻辑集中在统一组件中
4. **向后兼容**：对于标准 OAuth2 平台，自动使用标准处理逻辑
5. **动态配置兼容**：完全兼容现有的动态客户端配置流程

### 类比 UnifiedOAuth2UserService

就像 `UnifiedOAuth2UserService` 统一处理不同平台的用户信息获取一样，`UnifiedOAuth2AccessTokenResponseClient` 和 `UnifiedOAuth2AuthorizationRequestResolver` 统一处理不同平台的授权和 Token 获取，实现了一致的架构风格。