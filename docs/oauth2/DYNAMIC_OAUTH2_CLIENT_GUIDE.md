# 动态 OAuth2 客户端配置指南

## 概述

本指南介绍如何使用 Mortise 的动态 OAuth2 客户端功能。该功能支持在运行时动态添加、修改、删除 OAuth2 客户端配置，无需重启应用。

## 核心特性

### 1. **可选功能**
- OAuth2 功能是完全可选的，可以通过配置启用或禁用
- 当未启用时，应用仍然可以正常启动和运行（仅支持 JWT 认证）

### 2. **动态配置**
- 支持运行时从数据库加载客户端配置
- 配置变更无需重启应用
- 内置缓存机制，提升性能

### 3. **多客户端支持**
- 支持同时配置多个 OAuth2 客户端（如多个微信应用、GitHub、Google 等）
- 每个客户端使用唯一的 `registrationId` 标识

## 配置说明

### 1. 启用/禁用 OAuth2 功能

在 `application.yml` 或 `application.properties` 中配置：

```yaml
mortise:
  oauth2:
    # 是否启用动态 OAuth2 客户端功能（默认为 true）
    dynamic-client-enabled: true
```

或者：

```properties
mortise.oauth2.dynamic-client-enabled=true
```

### 2. 数据库表结构

执行以下 SQL 脚本创建配置表：

```sql
-- 位置: docs/sql/create_oauth2_client_config_table.sql
```

### 3. 添加 OAuth2 客户端配置

#### 方式一：直接插入数据库

```sql
INSERT INTO mortise_oauth2_client_config (
    id, registration_id, client_id, client_secret, client_name,
    scopes, redirect_uri_template, client_authentication_method,
    authorization_grant_type, authorization_uri, token_uri,
    user_info_uri, user_name_attribute, enabled
) VALUES (
    1, 'wechat_company_a', 'your-wechat-appid', 'your-wechat-secret', '微信登录（公司A）',
    'snsapi_login', '{baseUrl}/login/oauth2/code/{registrationId}',
    'client_secret_post', 'authorization_code',
    'https://open.weixin.qq.com/connect/qrconnect',
    'https://api.weixin.qq.com/sns/oauth2/access_token',
    'https://api.weixin.qq.com/sns/userinfo',
    'openid', TRUE
);
```

#### 方式二：通过管理后台（推荐）

在管理后台提供 CRUD 界面来管理 OAuth2 客户端配置。

## 使用流程

### 1. 用户登录

前端提供登录链接：

```html
<a href="/oauth2/authorization/wechat_company_a">使用微信登录（公司A）</a>
<a href="/oauth2/authorization/wechat_company_b">使用微信登录（公司B）</a>
<a href="/oauth2/authorization/github">使用 GitHub 登录</a>
```

### 2. 动态加载流程

```
用户点击登录链接
    ↓
Spring Security 拦截请求
    ↓
调用 DynamicClientRegistrationRepository.findByRegistrationId()
    ↓
1. 先查缓存
2. 缓存未命中，从数据库查询
3. 构建 ClientRegistration 对象
4. 放入缓存
    ↓
重定向到 OAuth2 提供者授权页面
    ↓
用户授权后回调
    ↓
Spring Security 获取 Token 和用户信息
    ↓
调用 OAuth2LoginSuccessHandler 处理登录成功逻辑
```

### 3. 缓存管理

当通过管理后台更新或删除客户端配置时，需要清除缓存：

```java
@Autowired
private DynamicClientRegistrationRepository dynamicRepository;

// 清除指定客户端的缓存
dynamicRepository.clearCache("wechat_company_a");

// 清除所有缓存
dynamicRepository.clearAllCache();
```

在集群环境中，建议通过消息队列（如 Redis Pub/Sub）通知所有节点清除缓存。

### 4. 预加载缓存（可选）

应用启动时预加载所有启用的客户端配置：

```java
@Component
@RequiredArgsConstructor
public class OAuth2CachePreloader implements ApplicationRunner {
    
    private final ObjectProvider<DynamicClientRegistrationRepository> repositoryProvider;
    
    @Override
    public void run(ApplicationArguments args) {
        repositoryProvider.ifAvailable(DynamicClientRegistrationRepository::preloadCache);
    }
}
```

## 架构设计

### 为什么使用 ObjectProvider？

```java
// OAuth2 相关的 Bean 都使用 ObjectProvider 注入
private final ObjectProvider<DynamicClientRegistrationRepository> dynamicClientRegistrationRepositoryProvider;
private final ObjectProvider<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandlerProvider;
```

**原因：**

1. **可选依赖**：OAuth2 是可选功能，Bean 可能不存在
2. **避免启动失败**：当 Bean 不存在时，应用仍能正常启动
3. **灵活配置**：通过配置文件控制功能启用/禁用
4. **向后兼容**：不影响只使用 JWT 认证的场景

### 组件说明

```
┌─────────────────────────────────────────────────────────────┐
│                   WebSecurityConfig                         │
│  - 条件化配置 OAuth2（使用 ObjectProvider）                  │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│          DynamicClientRegistrationRepository                │
│  - 实现 ClientRegistrationRepository 接口                    │
│  - 从数据库动态加载配置                                       │
│  - 内置缓存机制                                              │
│  - @ConditionalOnProperty 条件化创建                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│             Oauth2ClientConfigService                       │
│  - 基于 MyBatis-Flex 的数据访问层                            │
│  - 提供 CRUD 操作                                            │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                   Database Table                            │
│          mortise_oauth2_client_config                       │
└─────────────────────────────────────────────────────────────┘
```

## 常见问题

### Q1: 如何禁用 OAuth2 功能？

**A:** 在配置文件中设置：

```yaml
mortise:
  oauth2:
    dynamic-client-enabled: false
```

### Q2: 配置更新后为什么还是使用旧配置？

**A:** 需要清除缓存。有两种方式：

1. 在管理后台更新配置时自动调用 `clearCache()` 方法
2. 在集群环境中，通过消息队列通知所有节点清除缓存

### Q3: 如何支持新的 OAuth2 提供者？

**A:** 只需在数据库中插入新的配置记录即可，无需修改代码。

示例：添加 Google OAuth2 支持

```sql
INSERT INTO mortise_oauth2_client_config (
    registration_id, client_id, client_secret, client_name,
    scopes, redirect_uri_template,
    authorization_uri, token_uri, user_info_uri, user_name_attribute,
    enabled
) VALUES (
    'google', 'your-google-client-id', 'your-google-client-secret', 'Google',
    'openid,profile,email', '{baseUrl}/login/oauth2/code/{registrationId}',
    'https://accounts.google.com/o/oauth2/v2/auth',
    'https://oauth2.googleapis.com/token',
    'https://www.googleapis.com/oauth2/v3/userinfo',
    'sub', TRUE
);
```

### Q4: 如何监控缓存状态？

**A:** `DynamicClientRegistrationRepository` 提供了监控方法：

```java
// 获取当前缓存的客户端数量
int cacheSize = dynamicRepository.getCacheSize();
```

可以将此指标暴露给监控系统（如 Prometheus）。

## 最佳实践

### 1. 敏感信息加密

建议对 `client_secret` 进行加密存储：

```java
// 使用 Jasypt 等工具加密
@EncryptedColumn
private String clientSecret;
```

### 2. 配置审计

记录配置的创建、修改、删除操作：

```sql
ALTER TABLE mortise_oauth2_client_config 
ADD COLUMN created_by VARCHAR(100),
ADD COLUMN updated_by VARCHAR(100),
ADD COLUMN created_at TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP;
```

### 3. 集群部署

在集群环境中，使用 Redis Pub/Sub 同步缓存：

```java
@Component
@RequiredArgsConstructor
public class OAuth2CacheSynchronizer {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final DynamicClientRegistrationRepository repository;
    
    @PostConstruct
    public void subscribe() {
        redisTemplate.convertAndSend("oauth2:cache:clear", registrationId);
    }
    
    @RedisMessageListener(topic = "oauth2:cache:clear")
    public void handleCacheClear(String registrationId) {
        repository.clearCache(registrationId);
    }
}
```

### 4. 健康检查

提供健康检查端点：

```java
@Component
public class OAuth2HealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // 检查数据库连接、缓存状态等
        return Health.up()
            .withDetail("cacheSize", repository.getCacheSize())
            .build();
    }
}
```

## 总结

动态 OAuth2 客户端功能提供了一种灵活、可扩展的方式来管理多个 OAuth2 客户端配置。通过将配置存储在数据库中，可以实现运行时动态管理，无需修改代码或重启应用。同时，通过 `ObjectProvider` 和条件化配置，确保了该功能是完全可选的，不会影响不使用 OAuth2 的场景。
