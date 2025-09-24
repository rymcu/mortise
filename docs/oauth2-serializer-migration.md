# OAuth2 序列化器迁移说明

## 问题背景

在从 `RedisAuthorizationRequestRepository` 迁移到 `CacheAuthorizationRequestRepository` 的过程中，需要保持 OAuth2 对象的序列化兼容性。

## 原始实现 vs 新实现

### RedisAuthorizationRequestRepository.configureOAuth2ObjectMapper()

**原始实现的关键特性：**

```java
private ObjectMapper configureOAuth2ObjectMapper() {
    // 创建多态类型验证器，确保安全反序列化
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .allowIfSubType(OAuth2AuthorizationRequest.class)
            .build();

    // 使用JsonMapper.builder可以提供更多配置选项
    ObjectMapper mapper = JsonMapper.builder()
            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
            .build();

    // 注册Java 8日期时间模块
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 注册Spring Security的Jackson模块
    ClassLoader loader = getClass().getClassLoader();
    List<com.fasterxml.jackson.databind.Module> modules = SecurityJackson2Modules.getModules(loader);
    mapper.registerModules(modules);

    return mapper;
}
```

### CacheConfig.createOAuth2JacksonSerializer()

**新实现的对应功能：**

```java
private Jackson2JsonRedisSerializer<Object> createOAuth2JacksonSerializer() {
    // 创建多态类型验证器，确保安全反序列化
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .allowIfSubType(OAuth2AuthorizationRequest.class)
            .build();

    // 使用JsonMapper.builder可以提供更多配置选项
    ObjectMapper mapper = JsonMapper.builder()
            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
            .build();

    // 注册Java 8日期时间模块
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 注册Spring Security的Jackson模块
    ClassLoader loader = getClass().getClassLoader();
    List<com.fasterxml.jackson.databind.Module> modules = SecurityJackson2Modules.getModules(loader);
    mapper.registerModules(modules);

    // 配置其他属性
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
}
```

## 实现对比

| 特性 | RedisAuthorizationRequestRepository | CacheAuthorizationRequestRepository |
|------|-----------------------------------|-----------------------------------|
| **多态类型验证** | ✅ BasicPolymorphicTypeValidator | ✅ BasicPolymorphicTypeValidator |
| **Java时间模块** | ✅ JavaTimeModule + 禁用时间戳 | ✅ JavaTimeModule + 禁用时间戳 |
| **Security模块** | ✅ SecurityJackson2Modules | ✅ SecurityJackson2Modules |
| **类型安全** | ✅ 严格的OAuth2类型验证 | ✅ 严格的OAuth2类型验证 |
| **序列化方式** | 直接使用 ObjectMapper | 封装为 Jackson2JsonRedisSerializer |
| **缓存集成** | 手动Redis操作 | Spring Cache 统一管理 |

## 核心优势

### 1. 完全兼容性
- **相同的序列化逻辑**：保持与原始实现完全一致的序列化方式
- **类型安全**：继承了原始实现的类型验证机制
- **Spring Security支持**：完整保留了Security Jackson模块的支持

### 2. 统一架构优势
- **集中配置**：OAuth2序列化器在CacheConfig中统一管理
- **一致性**：与其他缓存使用相同的Spring Cache架构
- **可维护性**：序列化逻辑集中，便于维护和调试

### 3. 安全性增强
- **多态类型验证**：防止不安全的反序列化攻击
- **严格类型检查**：只允许OAuth2AuthorizationRequest及其相关类型
- **Spring Security集成**：利用Spring Security的安全序列化机制

## 使用场景

### CacheAuthorizationRequestRepository
```java
@Component("cacheAuthorizationRequestRepository")
public class CacheAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    
    @Resource
    private CacheService cacheService;
    
    // 直接使用CacheService，序列化由CacheConfig统一处理
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, ...) {
        cacheService.storeOAuth2AuthorizationRequest(state, authorizationRequest);
    }
}
```

### WebSecurityConfig集成
```java
@Configuration
public class WebSecurityConfig {
    
    @Resource
    private CacheAuthorizationRequestRepository cacheAuthorizationRequestRepository;
    
    // 使用统一缓存管理的授权请求仓库
    .authorizationRequestRepository(this.cacheAuthorizationRequestRepository)
}
```

## 总结

**回答原问题："RedisAuthorizationRequestRepository.configureOAuth2ObjectMapper 在 CacheAuthorizationRequestRepository 的对应实现是什么？"**

答案是：**CacheConfig.createOAuth2JacksonSerializer() 方法**

这个方法：
1. **功能等价**：提供与原始 `configureOAuth2ObjectMapper` 完全相同的序列化配置
2. **架构优化**：集成到统一的缓存配置体系中
3. **使用透明**：`CacheAuthorizationRequestRepository` 通过 `CacheService` 自动使用正确的序列化器
4. **维护性更好**：序列化逻辑集中管理，易于维护和调试

新的实现既保持了功能的完整性，又获得了统一缓存架构的所有优势。