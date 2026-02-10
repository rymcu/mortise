# mortise-system OAuth2 实现优化总结

## 优化目标

优化 mortise-system 模块的 OAuth2 实现，采用新的 SPI 策略模式，支持与 mortise-member 共用同一个 OAuth2 提供商（Logto）但使用不同的 registrationId 进行区分。

## 优化内容

### 1. 核心架构调整

#### 1.1 引入 StandardOAuth2UserInfo
- **位置**: `mortise-auth/spi/StandardOAuth2UserInfo.java`
- **用途**: 统一的 OAuth2 用户信息标准模型
- **字段**: provider, openId, nickname, email, avatar
- **优势**: 解耦第三方平台差异，提供统一接口

#### 1.2 创建 OAuth2AuthenticationContext  
- **位置**: `mortise-auth/context/OAuth2AuthenticationContext.java`
- **用途**: 携带 OAuth2 认证上下文信息
- **功能**: 
  - 根据 registrationId 自动识别模块类型（SYSTEM/MEMBER）
  - 提供 `isSystemModule()` 和 `isMemberModule()` 判断方法
  - 支持 `fromRegistrationId()` 工厂方法

#### 1.3 更新 LogtoProviderStrategy
- **修改**: `supports()` 方法支持所有 "logto*" 开头的 registrationId
- **原理**: 使用 `registrationId.startsWith("logto")` 模式匹配
- **效果**: 同时支持 `logto-admin` 和 `logto-member`

#### 1.4 增强 OAuth2UserInfoExtractor
- **修改**: 将 registrationId 保存到 `StandardOAuth2UserInfo.provider` 字段
- **原因**: 需要在后续流程中区分不同的 registrationId
- **影响**: 所有策略都能正确处理 registrationId

### 2. AuthService 接口升级

#### 2.1 新增方法

```java
/**
 * 从 OAuth2 用户信息查找或创建用户
 * @param userInfo 标准化的 OAuth2 用户信息
 * @param context OAuth2 认证上下文
 * @return 用户实体
 */
User findOrCreateUserFromOAuth2(StandardOAuth2UserInfo userInfo, OAuth2AuthenticationContext context);

/**
 * 为用户生成访问令牌和刷新令牌
 * @param user 用户实体
 * @return 包含 token 和 refreshToken 的对象
 */
TokenUser generateTokens(User user);
```

#### 2.2 废弃旧方法
```java
@Deprecated
TokenUser oauth2Login(OidcUser oidcUser, String registrationId);
```

### 3. AuthServiceImpl 实现优化

#### 3.1 findOrCreateUserFromOAuth2 实现

**核心逻辑**:
1. 通过 `provider + openId` 查找已存在用户
2. 如果存在，更新用户信息后返回
3. 如果不存在，尝试通过邮箱匹配现有用户
4. 如果匹配成功，关联 OAuth2 信息
5. 如果都不存在，创建新用户

**亮点**:
- 支持并发创建场景（捕获 `DataIntegrityViolationException`）
- 自动更新用户信息（昵称、邮箱、头像）
- 保留旧方法兼容性

#### 3.2 generateTokens 实现
```java
@Override
public TokenUser generateTokens(User user) {
    return generateAndStoreTokens(user);
}
```

#### 3.3 新增辅助方法

**createNewUserFromOAuth2**:
```java
private User createNewUserFromOAuth2(StandardOAuth2UserInfo userInfo) {
    User newUser = new User();
    newUser.setNickname(userService.checkNickname(userInfo.getNickname()));
    newUser.setAccount(userService.nextAccount());
    newUser.setEmail(userInfo.getEmail());
    newUser.setAvatar(StringUtils.isNotBlank(userInfo.getAvatar()) ? 
        userInfo.getAvatar() : DEFAULT_AVATAR);
    newUser.setOpenId(userInfo.getOpenId());
    newUser.setProvider(userInfo.getProvider());
    newUser.setCreatedTime(LocalDateTime.now());
    return newUser;
}
```

**updateExistingUser**:
```java
private void updateExistingUser(User existingUser, StandardOAuth2UserInfo userInfo) {
    boolean needsUpdate = false;
    // 按需更新昵称、邮箱、头像
    if (needsUpdate) {
        userService.updateById(existingUser);
    }
}
```

### 4. 创建 SystemOAuth2LoginSuccessHandler

#### 4.1 核心功能
- **位置**: `mortise-system/handler/SystemOAuth2LoginSuccessHandler.java`
- **职责**: 处理管理端 OAuth2 登录成功事件
- **流程**:
  1. 从 Authentication 提取 OAuth2User 和 registrationId
  2. 使用 OAuth2UserInfoExtractor 提取标准化用户信息
  3. 创建 OAuth2AuthenticationContext 上下文
  4. 调用 AuthService.findOrCreateUserFromOAuth2() 查找或创建用户
  5. 调用 AuthService.generateTokens() 生成令牌
  6. 返回 JSON 响应（包含 token 和 refreshToken）

#### 4.2 错误处理
```java
catch (Exception e) {
    log.error("OAuth2 登录失败: registrationId={}, error={}", registrationId, e.getMessage(), e);
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.getWriter().write(objectMapper.writeValueAsString(
        GlobalResult.error(message)
    ));
}
```

### 5. WebSecurityConfig 增强

#### 5.1 自定义 OidcUserService
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

## 优化效果

### 1. 架构优势
- ✅ **统一接口**: 所有 OAuth2 提供商使用统一的 `StandardOAuth2UserInfo`
- ✅ **灵活扩展**: 支持同一提供商多个 registrationId（如 logto-admin、logto-member）
- ✅ **模块解耦**: 策略模式降低模块间耦合
- ✅ **代码复用**: mortise-system 和 mortise-member 共用策略层

### 2. 功能提升
- ✅ **自动匹配**: 通过邮箱自动关联现有用户
- ✅ **信息同步**: 每次登录自动更新用户信息
- ✅ **并发安全**: 处理并发创建用户场景
- ✅ **上下文感知**: 根据 registrationId 自动识别模块类型

### 3. 可维护性
- ✅ **清晰职责**: Handler、Service、Strategy 各司其职
- ✅ **易于测试**: 各层逻辑独立，便于单元测试
- ✅ **日志完善**: 关键步骤都有详细日志
- ✅ **错误处理**: 统一的异常处理和错误响应

## 配置要点

### 1. application.yml 配置
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          logto-admin:
            provider: logto-admin
            client-id: ${LOGTO_ADMIN_CLIENT_ID}
            client-secret: ${LOGTO_ADMIN_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/login/oauth2/code/logto-admin"
```

### 2. 数据库准备
确保 `mortise_user` 表包含以下字段：
- `provider` VARCHAR(50) - 存储 registrationId
- `open_id` VARCHAR(100) - 存储第三方平台用户ID
- 建立唯一索引: `UNIQUE(provider, open_id)`

## 使用示例

### 管理端登录流程
```
1. 用户访问: /oauth2/authorization/logto-admin
2. Spring Security 跳转到 Logto 授权页面
3. 用户授权后回调: /login/oauth2/code/logto-admin
4. SystemOAuth2LoginSuccessHandler.onAuthenticationSuccess() 被调用
5. 提取用户信息 -> 创建上下文 -> 查找/创建用户 -> 生成令牌
6. 返回: {"success": true, "data": {"token": "...", "refreshToken": "..."}}
```

### 前端处理
```javascript
// 跳转到 OAuth2 授权
window.location.href = '/oauth2/authorization/logto-admin';

// 授权回调处理（在回调页面）
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');
const refreshToken = urlParams.get('refreshToken');
localStorage.setItem('token', token);
localStorage.setItem('refreshToken', refreshToken);
```

## 兼容性说明

### 1. 向后兼容
- 保留了旧的 `oauth2Login(OidcUser, String)` 方法
- 标记为 `@Deprecated`，内部调用新方法
- 不影响现有功能

### 2. 迁移建议
- 逐步替换旧方法调用为新方法
- 测试所有 OAuth2 登录流程
- 验证用户数据正确性

## 后续优化建议

### 1. 性能优化
- [ ] 添加用户信息缓存（减少数据库查询）
- [ ] 优化并发场景的重试逻辑
- [ ] 考虑使用分布式锁防止重复创建

### 2. 功能增强
- [ ] 支持 OAuth2 账号绑定/解绑
- [ ] 支持一个用户绑定多个 OAuth2 账号
- [ ] 添加 OAuth2 登录审计日志

### 3. 安全加固
- [ ] 添加 registrationId 白名单验证
- [ ] 限制登录频次（防暴力破解）
- [ ] 增强令牌安全性（如添加设备指纹）

## 相关文档

- [OAuth2 双 Logto 配置指南](./oauth2-dual-logto-configuration.md)
- [OAuth2 多提供商扩展设计](./oauth2-multi-provider-extension.md)
- [OAuth2 SPI 架构说明](./oauth2-spi-architecture.md)

## 变更文件清单

### 新增文件
1. `mortise-auth/context/OAuth2AuthenticationContext.java` - OAuth2 认证上下文
2. `mortise-system/handler/SystemOAuth2LoginSuccessHandler.java` - 登录成功处理器
3. `docs/oauth2-dual-logto-configuration.md` - 配置指南
4. `docs/oauth2-system-optimization-summary.md` - 本文档

### 修改文件
1. `mortise-auth/strategy/LogtoProviderStrategy.java` - 支持多 registrationId
2. `mortise-auth/service/OAuth2UserInfoExtractor.java` - 保存 registrationId
3. `mortise-system/service/AuthService.java` - 新增接口方法
4. `mortise-system/service/impl/AuthServiceImpl.java` - 实现新方法
5. `mortise-system/config/WebSecurityConfig.java` - 自定义 oidcUserService

## 测试验证

### 单元测试
- [ ] `LogtoProviderStrategy.supports()` 测试
- [ ] `OAuth2AuthenticationContext.fromRegistrationId()` 测试
- [ ] `AuthServiceImpl.findOrCreateUserFromOAuth2()` 测试
- [ ] `AuthServiceImpl.generateTokens()` 测试

### 集成测试
- [ ] 管理端 Logto 登录流程测试
- [ ] 用户端 Logto 登录流程测试（mortise-member）
- [ ] 并发登录测试
- [ ] 错误处理测试

### 回归测试  
- [ ] 原有 OAuth2 登录功能不受影响
- [ ] 用户数据完整性验证
- [ ] 令牌生成和验证正常

## 总结

本次优化成功将 mortise-system 模块的 OAuth2 实现升级为基于 SPI 策略模式的新架构，具备以下特点：

1. **架构清晰**: Handler -> Service -> Strategy 三层架构
2. **扩展性强**: 支持多提供商、多配置
3. **代码复用**: 策略层跨模块共享
4. **维护性好**: 职责清晰、易于测试

这为后续支持更多 OAuth2 提供商和功能扩展奠定了坚实基础。
