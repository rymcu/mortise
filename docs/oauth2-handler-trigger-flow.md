# SystemOAuth2LoginSuccessHandler 触发流程详解

## 📋 概述

`SystemOAuth2LoginSuccessHandler` 是系统管理端的 OAuth2 登录成功处理器，但**目前并未被实际使用**。

实际生效的是 `mortise-auth` 模块的 `OAuth2LoginSuccessHandler`（旧版）。

## 🔄 完整的 OAuth2 登录流程

### 1. 用户发起 OAuth2 登录

```
用户访问: /oauth2/authorization/logto
         ↓
Spring Security OAuth2 Client 拦截
         ↓
构建 OAuth2 授权请求
         ↓
重定向到 Logto 认证服务器
```

**配置位置**: `application-dev.yml`
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          logto:  # registrationId
            client-id: ${LOGTO_CLIENT_ID}
            client-secret: ${LOGTO_CLIENT_SECRET}
            redirect-uri: ${baseUrl}/api/oauth2/code/logto
            scope: openid,profile,offline_access,email
        provider:
          logto:
            issuer-uri: https://auth.atdak.com/oidc
```

### 2. 用户在 Logto 完成认证

```
用户在 Logto 登录
         ↓
Logto 验证用户身份
         ↓
重定向回应用: /api/oauth2/code/logto?code=xxx&state=xxx
```

### 3. Spring Security 处理回调

```
Spring Security OAuth2LoginAuthenticationFilter 拦截
         ↓
使用 authorization_code 换取 access_token 和 id_token
         ↓
解析 id_token 获取用户信息 (OidcUser)
         ↓
创建 OAuth2AuthenticationToken
         ↓
触发 AuthenticationSuccessHandler
```

**重定向端点配置**: `WebSecurityConfig.java`
```java
.redirectionEndpoint(redirection ->
    redirection.baseUri("/api/v1/oauth2/code/*"))
```

**实际处理路径**: `/api/v1/oauth2/code/logto` (注意前缀 `/api/v1`)

### 4. 触发成功处理器

**当前配置**: `WebSecurityConfig.java`
```java
http.oauth2Login(oauth2Login ->
    oauth2Login
        .successHandler(oauth2LoginSuccessHandler())  // ← 这里
);

@Bean
public OAuth2LoginSuccessHandler oauth2LoginSuccessHandler() {
    return new OAuth2LoginSuccessHandler();  // ← 使用的是 auth 模块的
}
```

**实际被调用的**: `OAuth2LoginSuccessHandler` (mortise-auth 模块)
- 路径: `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LoginSuccessHandler.java`
- 行为: 重定向到 `/api/v1/auth/oauth2/callback?registrationId=logto`

**未被使用的**: `SystemOAuth2LoginSuccessHandler` (mortise-system 模块)
- 路径: `mortise-system/src/main/java/com/rymcu/mortise/system/handler/SystemOAuth2LoginSuccessHandler.java`
- Bean 名称: `systemOAuth2LoginSuccessHandler`
- 状态: ⚠️ **已创建但未配置到 Spring Security**

## 🔧 如何启用 SystemOAuth2LoginSuccessHandler？

### 方案 1: 修改 WebSecurityConfig（推荐）

**修改**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java`

```java
@Resource(name = "systemOAuth2LoginSuccessHandler")  // ← 注入 system 模块的 Handler
private AuthenticationSuccessHandler systemOAuth2LoginSuccessHandler;

@Override
protected void configure(HttpSecurity http) throws Exception {
    // ...
    
    if (clientRegistrationRepository != null) {
        http.oauth2Login(oauth2Login ->
            oauth2Login
                .successHandler(systemOAuth2LoginSuccessHandler)  // ← 使用 system 模块的
        );
    }
}

// 删除或注释掉这个 Bean 定义
// @Bean
// public OAuth2LoginSuccessHandler oauth2LoginSuccessHandler() {
//     return new OAuth2LoginSuccessHandler();
// }
```

### 方案 2: SPI 模式（更灵活）

创建 `AuthenticationSuccessHandlerProvider` SPI 接口：

```java
package com.rymcu.mortise.auth.spi;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * OAuth2 登录成功处理器提供者 SPI
 */
public interface AuthenticationSuccessHandlerProvider {
    /**
     * 提供登录成功处理器
     */
    AuthenticationSuccessHandler getSuccessHandler();
    
    /**
     * 优先级
     */
    default int getOrder() {
        return 0;
    }
}
```

在 `mortise-system` 模块实现：

```java
@Component
public class SystemAuthenticationSuccessHandlerProvider implements AuthenticationSuccessHandlerProvider {
    
    @Resource
    private SystemOAuth2LoginSuccessHandler systemOAuth2LoginSuccessHandler;
    
    @Override
    public AuthenticationSuccessHandler getSuccessHandler() {
        return systemOAuth2LoginSuccessHandler;
    }
    
    @Override
    public int getOrder() {
        return 100; // 高优先级
    }
}
```

在 `WebSecurityConfig` 中使用：

```java
@Autowired(required = false)
private List<AuthenticationSuccessHandlerProvider> handlerProviders;

private AuthenticationSuccessHandler getOAuth2SuccessHandler() {
    if (handlerProviders != null && !handlerProviders.isEmpty()) {
        return handlerProviders.stream()
            .sorted(Comparator.comparingInt(AuthenticationSuccessHandlerProvider::getOrder).reversed())
            .findFirst()
            .map(AuthenticationSuccessHandlerProvider::getSuccessHandler)
            .orElseGet(OAuth2LoginSuccessHandler::new);
    }
    return new OAuth2LoginSuccessHandler();
}
```

## 📊 当前架构对比

### 当前状态（旧版）

```
OAuth2 登录成功
         ↓
OAuth2LoginSuccessHandler (auth 模块)
         ↓
重定向到 /api/v1/auth/oauth2/callback
         ↓
AuthController.oauth2Callback() (system 模块)
         ↓
调用 AuthService.oauth2Login()
         ↓
返回 JWT Token
```

**问题**:
- ❌ 多了一次重定向（性能损耗）
- ❌ OidcUser 信息在重定向后丢失
- ❌ 需要手动传递 registrationId

### 新架构（已实现但未启用）

```
OAuth2 登录成功
         ↓
SystemOAuth2LoginSuccessHandler (system 模块)
         ↓
直接调用 AuthService.findOrCreateUserFromOAuth2()
         ↓
返回 JWT Token JSON 响应
```

**优势**:
- ✅ 减少一次重定向
- ✅ 直接返回 JSON 响应（适合前后端分离）
- ✅ 信息不丢失，流程清晰
- ✅ 使用标准化的 StandardOAuth2UserInfo

## 🎯 推荐方案

### 立即行动

**修改 `WebSecurityConfig.java`**:

```java
@Configuration
public class WebSecurityConfig {
    
    // 注入 system 模块的 Handler
    @Resource(name = "systemOAuth2LoginSuccessHandler")
    private AuthenticationSuccessHandler systemOAuth2LoginSuccessHandler;
    
    // 删除旧的 Bean 定义
    // @Bean
    // public OAuth2LoginSuccessHandler oauth2LoginSuccessHandler() {
    //     return new OAuth2LoginSuccessHandler();
    // }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ...
        
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2Login ->
                oauth2Login
                    // ... 其他配置 ...
                    .successHandler(systemOAuth2LoginSuccessHandler)  // ← 启用新 Handler
            );
        }
        
        return http.build();
    }
}
```

### 清理工作

1. **删除旧的 Handler**: `OAuth2LoginSuccessHandler.java` (mortise-auth)
2. **删除回调端点**: `AuthController.oauth2Callback()` (system 模块)
3. **删除旧方法**: `AuthService.oauth2Login()` (已标记为 @Deprecated)

## 🧪 测试验证

### 测试步骤

1. **启动应用**:
   ```bash
   mvn spring-boot:run
   ```

2. **访问 OAuth2 登录**:
   ```
   GET http://localhost:8080/oauth2/authorization/logto
   ```

3. **在 Logto 完成登录**

4. **查看日志**:
   ```
   系统管理员 OAuth2 登录成功处理器被调用
   系统管理员 OAuth2 登录: registrationId=logto, provider=logto, openId=xxx, email=xxx
   系统管理员 OAuth2 登录成功: userId=xxx, account=xxx, provider=logto
   ```

5. **验证响应**:
   ```json
   {
     "success": true,
     "data": {
       "accessToken": "eyJhbGc...",
       "refreshToken": "eyJhbGc...",
       "tokenType": "Bearer",
       "expiresIn": 3600
     }
   }
   ```

## 📝 补充说明

### registrationId 的作用

- **registrationId**: Spring Security OAuth2 Client 的客户端注册标识
  - 例如: `logto`, `logto-admin`, `logto-member`, `github`, `google`
  
- **存储位置**: 
  - `StandardOAuth2UserInfo.provider` 字段
  - `mortise_user.provider` 数据库字段
  
- **用途**:
  - 区分不同的 OAuth2 Provider（Logto、GitHub、Google）
  - 区分同一 Provider 的不同用途（logto-admin vs logto-member）
  - 查询和绑定用户账号

### 双 Logto 配置示例

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          logto-admin:  # 管理后台
            client-id: ${LOGTO_ADMIN_CLIENT_ID}
            redirect-uri: ${baseUrl}/api/v1/oauth2/code/logto-admin
            scope: openid,profile,email
            provider: logto
          
          logto-member:  # 用户端
            client-id: ${LOGTO_MEMBER_CLIENT_ID}
            redirect-uri: ${baseUrl}/api/v1/oauth2/code/logto-member
            scope: openid,profile,email
            provider: logto
        
        provider:
          logto:
            issuer-uri: https://auth.atdak.com/oidc
```

访问入口:
- 管理后台: `/oauth2/authorization/logto-admin`
- 用户端: `/oauth2/authorization/logto-member`

## 🎊 总结

1. **SystemOAuth2LoginSuccessHandler 已实现但未启用**
2. **需要修改 WebSecurityConfig 才能生效**
3. **新架构更简洁、高效、符合最佳实践**
4. **建议立即切换到新 Handler**

---

**创建时间**: 2025-10-04  
**文档状态**: ✅ 已完成  
**相关文档**: 
- `oauth2-dual-logto-configuration.md`
- `oauth2-ultimate-simplification.md`
- `oauth2-ultimate-simplification-completed.md`
