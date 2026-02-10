# OAuth2 授权请求缓存服务迁移文档

## 概述

本文档记录了将 OAuth2 授权请求缓存操作从直接使用 `CacheService` 迁移到使用 `AuthCacheService` 的过程，遵循了项目的分层架构和模块化设计原则。

## 背景

根据 GitHub Remote 仓库的实现 ([CacheServiceImpl.java](https://github.com/rymcu/mortise/blob/master/src/main/java/com/rymcu/mortise/service/impl/CacheServiceImpl.java))，OAuth2 授权请求的缓存操作应该封装在业务缓存服务中，而不是直接使用基础设施层的 `CacheService`。

## 架构原则

### 分层架构
- **基础设施层**: `CacheService` - 提供底层缓存操作能力（Redis）
- **业务服务层**: `AuthCacheService` - 封装认证授权相关的缓存业务逻辑
- **应用层**: 各种 Repository、Handler 等 - 使用业务缓存服务

### 优势
1. **职责分离**: 业务逻辑与基础设施分离
2. **可维护性**: 缓存键命名、过期时间等配置集中管理
3. **可测试性**: 便于 Mock 和单元测试
4. **可扩展性**: 方便后续添加缓存预热、降级等高级功能

## 实施内容

### 1. 添加缓存常量

在 `AuthCacheConstant` 中定义 OAuth2 授权请求相关的缓存配置：

```java
/**
 * OAuth2 授权请求缓存
 */
public static final String OAUTH2_AUTHORIZATION_REQUEST_CACHE = "oauth2:auth-request";
public static final long OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES = 10;
```

**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/constant/AuthCacheConstant.java`

### 2. 扩展 AuthCacheService 接口

在 `AuthCacheService` 接口中添加 OAuth2 授权请求缓存操作方法：

```java
// ==================== OAuth2 授权请求缓存操作 ====================

/**
 * 存储 OAuth2 授权请求
 *
 * @param state                OAuth2 state 参数
 * @param authorizationRequest OAuth2 授权请求对象
 */
void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest);

/**
 * 获取 OAuth2 授权请求
 *
 * @param state OAuth2 state 参数
 * @param clazz 授权请求对象类型
 * @param <T>   授权请求对象泛型
 * @return OAuth2 授权请求对象，如果不存在返回null
 */
<T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz);

/**
 * 删除 OAuth2 授权请求
 *
 * @param state OAuth2 state 参数
 */
void removeOAuth2AuthorizationRequest(String state);
```

**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/service/AuthCacheService.java`

### 3. 实现 OAuth2 缓存操作

在 `AuthCacheServiceImpl` 中实现 OAuth2 授权请求缓存操作：

```java
@Override
public void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest) {
    String cacheKey = AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE + ":" + state;
    cacheService.set(cacheKey, authorizationRequest, 
                    AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES, 
                    TimeUnit.MINUTES);
    log.debug("存储 OAuth2 授权请求：{}", state);
}

@Override
public <T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz) {
    String cacheKey = AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE + ":" + state;
    T request = cacheService.get(cacheKey, clazz);
    log.debug("获取 OAuth2 授权请求：{} -> {}", state, request != null ? "存在" : "不存在");
    return request;
}

@Override
public void removeOAuth2AuthorizationRequest(String state) {
    String cacheKey = AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE + ":" + state;
    cacheService.delete(cacheKey);
    log.debug("删除 OAuth2 授权请求：{}", state);
}
```

**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/service/impl/AuthCacheServiceImpl.java`

### 4. 更新 CacheAuthorizationRequestRepository

重构 `CacheAuthorizationRequestRepository` 使用 `AuthCacheService` 替代直接使用 `CacheService`：

**修改前**:
```java
@Resource
private CacheService cacheService;

private OAuth2AuthorizationRequest getAuthorizationRequest(String state) {
    String cacheKey = getCacheKey(state);
    Object cached = cacheService.get(cacheKey);
    // ...
}
```

**修改后**:
```java
@Resource
private AuthCacheService authCacheService;

private OAuth2AuthorizationRequest getAuthorizationRequest(String state) {
    OAuth2AuthorizationRequest request = authCacheService.getOAuth2AuthorizationRequest(
            state, OAuth2AuthorizationRequest.class);
    log.debug("从缓存加载 OAuth2 授权请求: state={} -> {}", state, request != null ? "存在" : "不存在");
    return request;
}
```

**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/repository/CacheAuthorizationRequestRepository.java`

### 5. 创建 JwtAuthenticationEntryPoint

创建 JWT 认证入口点，处理未认证访问：

```java
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.warn("未认证访问: {} - {}", request.getRequestURI(), authException.getMessage());
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ServletOutputStream outputStream = response.getOutputStream();
        GlobalResult<Object> result = GlobalResult.error(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        outputStream.write(objectMapper.writeValueAsBytes(result));
        outputStream.flush();
        outputStream.close();
    }
}
```

**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/JwtAuthenticationEntryPoint.java`

## 文件清单

### 新增文件
1. `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/JwtAuthenticationEntryPoint.java`
2. `mortise-auth/src/main/java/com/rymcu/mortise/auth/repository/CacheAuthorizationRequestRepository.java`
3. `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LoginSuccessHandler.java`
4. `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LogoutSuccessHandler.java`
5. `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/RewriteAccessDeniedHandler.java`

### 修改文件
1. `mortise-auth/src/main/java/com/rymcu/mortise/auth/service/AuthCacheService.java`
   - 添加 OAuth2 授权请求缓存操作方法

2. `mortise-auth/src/main/java/com/rymcu/mortise/auth/service/impl/AuthCacheServiceImpl.java`
   - 实现 OAuth2 授权请求缓存操作

3. `mortise-auth/src/main/java/com/rymcu/mortise/auth/constant/AuthCacheConstant.java`
   - 已包含 OAuth2 授权请求缓存配置常量

## 测试验证

### 编译测试
```bash
mvn clean compile -DskipTests
```

**结果**: ✅ BUILD SUCCESS

### 功能验证要点

1. **OAuth2 授权流程**
   - 用户访问 OAuth2 登录端点
   - 授权请求被存储到 Redis（通过 `AuthCacheService`）
   - 回调时能正确读取并删除授权请求

2. **缓存键格式**
   - 格式: `oauth2:auth-request:{state}`
   - 过期时间: 10分钟

3. **日志记录**
   - 所有缓存操作都有 DEBUG 级别日志
   - 便于排查 OAuth2 授权流程问题

## 注意事项

1. **缓存序列化**: `OAuth2AuthorizationRequest` 对象需要能被正确序列化/反序列化
2. **过期时间**: 10分钟的过期时间足够完成 OAuth2 授权流程
3. **并发安全**: Redis 操作本身是原子的，无需额外同步
4. **缓存清理**: 授权请求在使用后会被删除，避免内存泄漏

## 参考资源

- GitHub 原始实现: [CacheServiceImpl.java](https://github.com/rymcu/mortise/blob/master/src/main/java/com/rymcu/mortise/service/impl/CacheServiceImpl.java)
- Spring Security OAuth2 文档: [OAuth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html)
- Redis 缓存最佳实践: [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/)

## 后续工作

1. ✅ 完成 OAuth2 授权请求缓存迁移
2. ✅ 创建 JWT 认证入口点
3. ✅ 编译验证通过
4. ⏳ 集成测试 OAuth2 登录流程
5. ⏳ 性能测试缓存操作
6. ⏳ 添加监控指标（缓存命中率等）

## 总结

本次迁移成功将 OAuth2 授权请求缓存操作从基础设施层提升到业务服务层，符合项目的分层架构设计原则。通过封装 `AuthCacheService`，使得缓存操作更加语义化、可维护，并为后续的功能扩展打下了良好的基础。

---

**文档版本**: 1.0  
**创建时间**: 2025-10-02  
**作者**: @ronger  
**状态**: ✅ 已完成
