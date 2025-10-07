# 动态 OAuth2 客户端实现总结

## 概述

成功实现了基于 MyBatis-Flex 的动态 OAuth2 客户端功能，支持运行时动态管理 OAuth2 客户端配置，无需重启应用。

## 关键设计决策

### 1. **OAuth2 是可选功能** ✅

**问题**：最初设计将 OAuth2 相关的 Bean 作为必需依赖直接注入，这会导致未配置 OAuth2 时应用无法启动。

**解决方案**：使用 `ObjectProvider<T>` 包装所有 OAuth2 相关的依赖。

```java
// ❌ 错误的方式：直接注入（会导致 Bean 不存在时启动失败）
private final DynamicClientRegistrationRepository dynamicClientRegistrationRepository;

// ✅ 正确的方式：使用 ObjectProvider（Bean 不存在时仍可启动）
private final ObjectProvider<DynamicClientRegistrationRepository> dynamicClientRegistrationRepositoryProvider;
```

### 2. **为什么使用 ObjectProvider？**

`ObjectProvider<T>` 是 Spring 提供的一种延迟、可选的依赖注入方式：

| 特性 | 直接注入 | ObjectProvider |
|------|---------|----------------|
| Bean 不存在时 | ❌ 启动失败 | ✅ 正常启动 |
| 获取方式 | 自动注入 | 手动调用 `getIfAvailable()` |
| 适用场景 | 必需依赖 | 可选依赖 |
| 条件判断 | 无法判断 | 可以判断是否存在 |

**示例：**

```java
// 获取可选的 Bean
DynamicClientRegistrationRepository repository = 
    dynamicClientRegistrationRepositoryProvider.getIfAvailable();

if (repository != null) {
    // Bean 存在，配置 OAuth2
    configureOAuth2Login(http, repository);
} else {
    // Bean 不存在，跳过配置
    log.info("未启用 OAuth2 功能");
}
```

### 3. **条件化 Bean 创建**

使用 `@ConditionalOnProperty` 注解控制 Bean 的创建：

```java
@Repository
@ConditionalOnProperty(
    prefix = "mortise.oauth2",
    name = "dynamic-client-enabled",
    havingValue = "true",
    matchIfMissing = true  // 默认启用
)
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {
    // ...
}
```

配置文件控制：

```yaml
mortise:
  oauth2:
    dynamic-client-enabled: false  # 禁用 OAuth2
```

## 实现的文件

### 1. 核心类

| 文件 | 说明 |
|------|------|
| `Oauth2ClientConfig.java` | OAuth2 客户端配置实体类 |
| `Oauth2ClientConfigMapper.java` | MyBatis-Flex Mapper |
| `Oauth2ClientConfigService.java` | 服务接口 |
| `Oauth2ClientConfigServiceImpl.java` | 服务实现 |
| `DynamicClientRegistrationRepository.java` | 动态客户端注册仓库 |
| `WebSecurityConfig.java` | 更新的安全配置（支持可选 OAuth2） |

### 2. 配置文件

| 文件 | 说明 |
|------|------|
| `create_oauth2_client_config_table.sql` | 数据库表创建脚本 |
| `DYNAMIC_OAUTH2_CLIENT_GUIDE.md` | 使用指南 |

### 3. 依赖更新

在 `mortise-auth/pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-spring-boot3-starter</artifactId>
</dependency>
```

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                  应用启动                                     │
└─────────────────────────────────────────────────────────────┘
                          ↓
         检查配置: mortise.oauth2.dynamic-client-enabled
                          ↓
                    ┌─────┴─────┐
                    │           │
                 true          false
                    │           │
                    ↓           ↓
    创建 DynamicClientRegistrationRepository    不创建 Bean
                    │                               │
                    ↓                               ↓
    WebSecurityConfig 检测到 Bean              跳过 OAuth2 配置
                    │                               │
                    ↓                               ↓
        启用 OAuth2 功能                    仅支持 JWT 认证
```

## 工作流程

### 1. 用户登录流程

```
用户访问: /oauth2/authorization/wechat_company_a
                          ↓
    DynamicClientRegistrationRepository.findByRegistrationId()
                          ↓
                  ┌───────┴───────┐
                  │               │
            缓存命中          缓存未命中
                  │               │
                  ↓               ↓
            返回配置        查询数据库
                              ↓
                      构建 ClientRegistration
                              ↓
                          放入缓存
                              ↓
                          返回配置
                              ↓
            重定向到 OAuth2 提供者授权页面
```

### 2. 配置更新流程

```
管理员更新配置
        ↓
保存到数据库
        ↓
调用 clearCache(registrationId)
        ↓
清除本地缓存
        ↓
（集群环境）发布 Redis 消息
        ↓
其他节点接收消息
        ↓
清除其他节点的缓存
```

## 关键代码解析

### 1. 条件化配置 OAuth2

```java
@Bean
@Order(100)
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // 基础配置（总是应用）
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // ... 其他配置
        
    // 条件化配置 OAuth2（可选）
    DynamicClientRegistrationRepository dynamicRepository = 
            dynamicClientRegistrationRepositoryProvider.getIfAvailable();

    if (dynamicRepository != null) {
        // 如果 Bean 存在，则配置 OAuth2
        configureOAuth2Login(http, dynamicRepository);
    } else {
        // 如果 Bean 不存在，则跳过
        log.info("未启用 OAuth2 功能");
    }
    
    return http.build();
}
```

### 2. 使用 ObjectProvider 配置处理器

```java
private void configureOAuth2Login(HttpSecurity http,
                                 ClientRegistrationRepository clientRegistrationRepository) {
    http.oauth2Login(oauth2Login -> {
        // ... 基础配置
        
        // 使用 ObjectProvider 配置可选的处理器
        unifiedOAuth2UserServiceProvider.ifAvailable(service ->
            oauth2Login.userInfoEndpoint(userInfo -> userInfo.userService(service))
        );
        
        oauth2LoginSuccessHandlerProvider.ifAvailable(oauth2Login::successHandler);
    });
    
    // 配置登出处理器（也是可选的）
    oauth2LogoutSuccessHandlerProvider.ifAvailable(handler -> {
        // ...
    });
}
```

### 3. 动态加载配置

```java
@Override
public ClientRegistration findByRegistrationId(String registrationId) {
    // 1. 优先从缓存获取
    ClientRegistration registration = registrationCache.get(registrationId);
    if (registration != null) {
        return registration;
    }

    // 2. 从数据库查询
    Optional<Oauth2ClientConfig> configOpt = 
        clientConfigService.findByRegistrationId(registrationId);

    if (configOpt.isPresent()) {
        // 3. 构建 ClientRegistration
        ClientRegistration newRegistration = buildClientRegistration(configOpt.get());
        
        // 4. 放入缓存
        registrationCache.put(registrationId, newRegistration);
        
        return newRegistration;
    }

    return null;
}
```

## 优势

### 1. **完全可选**
- OAuth2 功能可通过配置启用/禁用
- 不使用 OAuth2 时，应用仍可正常运行
- 向后兼容现有的 JWT 认证

### 2. **动态配置**
- 无需修改代码或重启应用
- 支持运行时添加/修改/删除客户端
- 适合多租户场景

### 3. **高性能**
- 内置缓存机制，避免重复查询数据库
- 按需加载，只加载实际使用的客户端配置
- 支持预加载常用配置

### 4. **易维护**
- 配置集中管理在数据库中
- 提供清晰的 API 和文档
- 支持配置审计和历史追踪

## 使用示例

### 启用 OAuth2

```yaml
# application.yml
mortise:
  oauth2:
    dynamic-client-enabled: true
```

### 添加微信客户端

```sql
INSERT INTO mortise_oauth2_client_config (
    registration_id, client_id, client_secret, client_name,
    scopes, redirect_uri_template,
    authorization_uri, token_uri, user_info_uri, user_name_attribute,
    enabled
) VALUES (
    'wechat_company_a', 'wx123456', 'secret123', '微信登录（公司A）',
    'snsapi_login', '{baseUrl}/login/oauth2/code/{registrationId}',
    'https://open.weixin.qq.com/connect/qrconnect',
    'https://api.weixin.qq.com/sns/oauth2/access_token',
    'https://api.weixin.qq.com/sns/userinfo',
    'openid', TRUE
);
```

### 前端登录链接

```html
<a href="/oauth2/authorization/wechat_company_a">使用微信登录</a>
```

## 后续优化建议

1. **管理界面**：开发 OAuth2 客户端配置的 CRUD 管理界面
2. **缓存同步**：在集群环境中实现跨节点缓存同步
3. **监控指标**：暴露缓存命中率、配置加载时间等指标
4. **配置验证**：添加配置有效性检查（如测试连接）
5. **安全加固**：对敏感字段（如 client_secret）进行加密存储

## 总结

通过使用 `ObjectProvider` 和条件化配置，成功实现了一个**灵活、可选、高性能**的动态 OAuth2 客户端管理系统。该设计既保证了功能的完整性，又确保了系统的灵活性和可维护性。

**核心要点**：
- ✅ OAuth2 是可选功能（使用 ObjectProvider）
- ✅ 支持运行时动态配置（数据库驱动）
- ✅ 高性能（内置缓存）
- ✅ 易扩展（支持多客户端）
- ✅ 向后兼容（不影响现有功能）
