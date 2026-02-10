# OAuth2 双 Logto 配置指南

## 概述

本文档描述如何配置 mortise-system（管理端）和 mortise-member（用户端）同时使用 Logto 作为 OAuth2 提供商，但使用不同的 `registrationId` 进行区分。

## 架构设计

### 1. 统一策略支持
- `LogtoProviderStrategy` 支持所有以 "logto" 开头的 registrationId
- 自动识别 `logto-admin`、`logto-member` 等不同配置

### 2. 上下文区分
- `OAuth2AuthenticationContext` 根据 registrationId 自动识别模块类型
- 支持 `isSystemModule()` 和 `isMemberModule()` 判断

### 3. 统一用户信息提取
- `OAuth2UserInfoExtractor` 自动选择合适的策略
- 保留 registrationId 信息用于后续处理

## 配置示例

### application.yml 配置

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # 管理端 Logto 配置
          logto-admin:
            provider: logto-admin
            client-id: ${LOGTO_ADMIN_CLIENT_ID:your-admin-client-id}
            client-secret: ${LOGTO_ADMIN_CLIENT_SECRET:your-admin-client-secret}
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/login/oauth2/code/logto-admin"
            authorization-grant-type: authorization_code
            client-name: Logto Admin

          # 用户端 Logto 配置  
          logto-member:
            provider: logto-member
            client-id: ${LOGTO_MEMBER_CLIENT_ID:your-member-client-id}
            client-secret: ${LOGTO_MEMBER_CLIENT_SECRET:your-member-client-secret}
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/login/oauth2/code/logto-member"
            authorization-grant-type: authorization_code
            client-name: Logto Member

        provider:
          # 管理端 Logto 提供商
          logto-admin:
            issuer-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc
            authorization-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/auth
            token-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/token
            user-info-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/me
            jwk-set-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/jwks
            user-name-attribute: sub

          # 用户端 Logto 提供商
          logto-member:
            issuer-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc
            authorization-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/auth
            token-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/token
            user-info-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/me
            jwk-set-uri: ${LOGTO_ENDPOINT:https://your-logto-instance.com}/oidc/jwks
            user-name-attribute: sub
```

### 环境变量配置

```bash
# Logto 实例地址
LOGTO_ENDPOINT=https://your-logto-instance.com

# 管理端应用配置
LOGTO_ADMIN_CLIENT_ID=admin-app-client-id
LOGTO_ADMIN_CLIENT_SECRET=admin-app-client-secret

# 用户端应用配置  
LOGTO_MEMBER_CLIENT_ID=member-app-client-id
LOGTO_MEMBER_CLIENT_SECRET=member-app-client-secret
```

## 登录流程

### 管理端登录
1. 访问：`/oauth2/authorization/logto-admin`
2. 跳转到 Logto 管理端应用授权页面
3. 用户授权后回调：`/login/oauth2/code/logto-admin`
4. `SystemOAuth2LoginSuccessHandler` 处理登录成功
5. 创建或更新 `mortise_user` 表用户
6. 返回管理端访问令牌

### 用户端登录
1. 访问：`/oauth2/authorization/logto-member`
2. 跳转到 Logto 用户端应用授权页面
3. 用户授权后回调：`/login/oauth2/code/logto-member`
4. `MemberOAuth2Service` 处理登录成功
5. 创建或更新 `mortise_member` 表用户
6. 返回用户端访问令牌

## 关键实现类

### 1. LogtoProviderStrategy
```java
@Override
public boolean supports(String registrationId) {
    return registrationId != null && registrationId.startsWith("logto");
}
```

### 2. OAuth2AuthenticationContext  
```java
public static OAuth2AuthenticationContext fromRegistrationId(String registrationId) {
    ModuleType moduleType = registrationId.startsWith("logto-admin") ? 
        ModuleType.SYSTEM : ModuleType.MEMBER;
    return new OAuth2AuthenticationContext(registrationId, moduleType);
}
```

### 3. SystemOAuth2LoginSuccessHandler
```java
// 提取用户信息
StandardOAuth2UserInfo userInfo = oAuth2UserInfoExtractor.extractUserInfo(
    oAuth2User, registrationId);

// 创建上下文
OAuth2AuthenticationContext context = OAuth2AuthenticationContext.fromRegistrationId(registrationId);

// 查找或创建用户
User user = authService.findOrCreateUserFromOAuth2(userInfo, context);

// 生成令牌
TokenUser tokenUser = authService.generateTokens(user);
```

## 数据库表结构

### mortise_user (管理端用户)
```sql
CREATE TABLE mortise_user (
    id BIGINT PRIMARY KEY,
    account VARCHAR(50) UNIQUE,
    nickname VARCHAR(100),
    email VARCHAR(100),
    avatar VARCHAR(500),
    provider VARCHAR(50),    -- 存储 'logto-admin'
    open_id VARCHAR(100),    -- 存储 Logto 的 sub
    created_time TIMESTAMP,
    UNIQUE(provider, open_id)
);
```

### mortise_member (用户端用户)
```sql
CREATE TABLE mortise_member (
    id BIGINT PRIMARY KEY,
    account VARCHAR(50) UNIQUE,
    nickname VARCHAR(100), 
    email VARCHAR(100),
    avatar VARCHAR(500),
    created_time TIMESTAMP
);

CREATE TABLE mortise_member_oauth2_binding (
    id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,    -- 存储 'logto-member'
    open_id VARCHAR(100) NOT NULL,    -- 存储 Logto 的 sub
    nickname VARCHAR(100),
    email VARCHAR(100),
    avatar VARCHAR(500),
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    UNIQUE(provider, open_id),
    FOREIGN KEY (member_id) REFERENCES mortise_member(id)
);
```

## 安全配置

### WebSecurityConfig 更新
```java
@Bean
public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    OidcUserService delegate = new OidcUserService();
    
    return (userRequest) -> {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // 将 registrationId 添加到用户属性中
        Map<String, Object> modifiedAttributes = new HashMap<>(oidcUser.getAttributes());
        modifiedAttributes.put("registrationId", registrationId);
        
        return new DefaultOidcUser(oidcUser.getAuthorities(), 
            oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub", modifiedAttributes);
    };
}
```

## 最佳实践

### 1. 错误处理
- 在 `SystemOAuth2LoginSuccessHandler` 中统一处理异常
- 返回 JSON 格式的错误响应，便于前端处理

### 2. 日志记录
- 记录用户登录、创建、更新等关键操作
- 包含 registrationId 和用户标识信息

### 3. 缓存策略
- OAuth2 用户信息可适当缓存，减少数据库查询
- 注意缓存失效策略

### 4. 安全考虑
- 验证 registrationId 的合法性
- 防止 CSRF 攻击
- 限制登录频次

## 测试验证

### 1. 功能测试
```bash
# 测试管理端登录
curl -i "http://localhost:8080/oauth2/authorization/logto-admin"

# 测试用户端登录  
curl -i "http://localhost:8080/oauth2/authorization/logto-member"
```

### 2. 集成测试
- 验证不同 registrationId 路由到正确的处理器
- 验证用户数据存储到正确的表
- 验证令牌生成和验证

## 故障排查

### 常见问题
1. **配置错误**：检查 client-id、client-secret 是否正确
2. **回调地址不匹配**：确保 Logto 应用配置的回调地址正确
3. **CORS 问题**：检查前端域名是否在 Logto 允许列表中
4. **用户信息缺失**：检查 scope 配置是否包含必要的权限

### 调试技巧
- 启用 Spring Security 调试日志
- 检查 OAuth2 授权流程各步骤的请求响应
- 验证 JWT 令牌的有效性

## 扩展支持

当前架构已支持扩展更多 OAuth2 提供商：
- GitHub: `github-admin`, `github-member`
- Google: `google-admin`, `google-member`  
- 微信: `wechat-admin`, `wechat-member`

只需要添加相应的配置和策略实现即可。