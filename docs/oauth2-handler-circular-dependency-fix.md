# OAuth2 Handler 循环依赖解决方案

## 🔴 问题描述

在实现 OAuth2 登录处理器 SPI 架构时，遇到了循环依赖问题：

```
┌─────┐
|  webSecurityConfig
↑     ↓
|  OAuth2LoginSuccessHandler
↑     ↓
|  systemOAuth2LoginSuccessHandlerProvider
↑     ↓
|  systemOAuth2LoginSuccessHandler
↑     ↓
|  authServiceImpl
↑     ↓
|  userServiceImpl
└─────┘
```

### 错误信息

```
Description:

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  webSecurityConfig
↑     ↓
|  OAuth2LoginSuccessHandler defined in file [.../OAuth2LoginSuccessHandler.class]
↑     ↓
|  systemOAuth2LoginSuccessHandler
↑     ↓
|  authServiceImpl
↑     ↓
|  userServiceImpl
└─────┘

Action:

Relying upon circular references is discouraged and they are prohibited by default. 
Update your application to remove the dependency cycle between beans. 
As a last resort, it may be possible to break the cycle automatically by 
setting spring.main.allow-circular-references to true.
```

## 🔍 根因分析

### 依赖链路

1. **WebSecurityConfig** → `OAuth2LoginSuccessHandler`
   - 通过 `@Resource` 注入

2. **OAuth2LoginSuccessHandler** → `SystemOAuth2LoginSuccessHandlerProvider`
   - 构造函数注入 `Optional<List<OAuth2LoginSuccessHandlerProvider>>`
   - ⚠️ **问题点**: 构造函数中调用 `providersOptional.get()` 会立即触发所有 Provider 的初始化

3. **SystemOAuth2LoginSuccessHandlerProvider** → `SystemOAuth2LoginSuccessHandler`
   - 虽然使用了 `ObjectProvider`，但 Provider 本身的创建仍会触发依赖链
   
4. **SystemOAuth2LoginSuccessHandler** → `AuthService`
   - 通过 `@Resource` 注入

5. **AuthServiceImpl** → `UserService`
   - 通过 `@Resource` 注入

6. **UserServiceImpl** → ??? (可能间接依赖了 WebSecurityConfig)

### 问题关键

虽然我们使用了 `ObjectProvider` 来延迟加载 `SystemOAuth2LoginSuccessHandler`，但问题在于：

```java
// ❌ 错误的实现
public OAuth2LoginSuccessHandler(Optional<List<...>> providersOptional) {
    if (providersOptional.isEmpty()) return;
    
    List<...> providers = providersOptional.get();  // ← 立即获取，触发 Provider 创建
    
    providers.forEach(provider -> {
        AuthenticationSuccessHandler handler = provider.getHandler();  // ← 进而触发 Handler 创建
        // 注册到路由表
    });
}
```

即使 `provider.getHandler()` 使用了 `ObjectProvider`，但 `Provider` 本身的创建就已经在依赖链中了。

## ✅ 解决方案

### 完整解决方案：三层延迟加载

**核心思想**: 使用三层延迟加载策略，确保在构造函数中不触发任何依赖链。

#### 第一层：WebSecurityConfig 构造函数注入 Optional

```java
@Configuration
public class WebSecurityConfig {
    
    // ❌ 错误：使用 @Resource 会在构造函数后立即注入，触发依赖链
    // @Resource
    // private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    
    // ✅ 正确：使用构造函数注入 Optional
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    
    @Autowired
    public WebSecurityConfig(
            Optional<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandlerOptional,
            // ... 其他依赖
    ) {
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandlerOptional.orElse(null);
        // 不调用任何会触发依赖链的方法
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // 这个方法在所有 Bean 创建后才调用，可以安全使用
        http.oauth2Login(oauth2 -> 
            oauth2.successHandler(oauth2LoginSuccessHandler)
        );
    }
}
```

#### 第二层：OAuth2LoginSuccessHandler 使用 @PostConstruct

#### 第二层：OAuth2LoginSuccessHandler 使用 @PostConstruct

```java
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final Map<String, AuthenticationSuccessHandler> handlerMap = new HashMap<>();
    private AuthenticationSuccessHandler defaultHandler;
    
    // 只保存 Provider 引用，不立即使用
    private final Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional;
    
    /**
     * 构造函数只保存引用，不初始化路由表
     */
    public OAuth2LoginSuccessHandler(
            Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional) {
        this.providersOptional = providersOptional;
        log.info("OAuth2LoginSuccessHandler 构造函数执行（延迟初始化路由表）");
    }
    
    /**
     * 在所有依赖注入完成后初始化路由表
     */
    @PostConstruct
    public void initializeRoutes() {
        log.info("OAuth2LoginSuccessHandler 初始化路由表（@PostConstruct）");
        
        if (providersOptional.isEmpty() || providersOptional.get().isEmpty()) {
            log.warn("未发现任何 OAuth2LoginSuccessHandlerProvider");
            return;
        }
        
        List<OAuth2LoginSuccessHandlerProvider> providers = providersOptional.get();
        
        // 按优先级排序并注册路由
        providers.stream()
            .filter(OAuth2LoginSuccessHandlerProvider::isEnabled)
            .sorted(Comparator.comparingInt(
                OAuth2LoginSuccessHandlerProvider::getOrder).reversed())
            .forEach(provider -> {
                // 此时调用 getHandler() 是安全的，因为所有 Bean 已经创建完成
                AuthenticationSuccessHandler handler = provider.getHandler();
                String[] registrationIds = provider.getSupportedRegistrationIds();
                
                for (String registrationId : registrationIds) {
                    handlerMap.put(registrationId, handler);
                }
                
                if (provider.isDefault()) {
                    defaultHandler = handler;
                }
            });
    }
    
    @Override
    public void onAuthenticationSuccess(...) {
        // 使用路由表
        String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();
        AuthenticationSuccessHandler handler = 
            handlerMap.getOrDefault(registrationId, defaultHandler);
        handler.onAuthenticationSuccess(request, response, authentication);
    }
}
```

#### 第三层：Provider 使用 ObjectProvider

```java
@Component
public class SystemOAuth2LoginSuccessHandlerProvider 
        implements OAuth2LoginSuccessHandlerProvider {
    
    private final ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider;
    
    public SystemOAuth2LoginSuccessHandlerProvider(
            ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }
    
    @Override
    public AuthenticationSuccessHandler getHandler() {
        // 在 @PostConstruct 阶段调用时，所有 Bean 已创建完成
        return handlerProvider.getObject();
    }
    
    @Override
    public String[] getSupportedRegistrationIds() {
        return new String[] { "logto", "logto-admin" };
    }
}
```

### Bean 初始化顺序（三层延迟加载）

```
阶段 1: 构造函数调用
┌────────────────────────────────────────┐
│ 1. WebSecurityConfig(Optional<OAuth2LoginSuccessHandler>) │
│    ↓ (保存 Optional 引用，不调用 get()) │
│                                        │
│ 2. OAuth2LoginSuccessHandler(Optional<List<Provider>>) │
│    ↓ (保存 Optional 引用，不调用 get()) │
│                                        │
│ 3. SystemOAuth2LoginSuccessHandlerProvider(ObjectProvider) │
│    ↓ (保存 ObjectProvider 引用)       │
│                                        │
│ 4. SystemOAuth2LoginSuccessHandler()   │
│    ↓ (注入 AuthService)               │
│                                        │
│ 5. AuthServiceImpl()                   │
│    ↓ (注入 UserService)               │
│                                        │
│ 6. UserServiceImpl()                   │
│    ↓                                   │
└────────────────────────────────────────┘
         ↓
阶段 2: @PostConstruct 调用
┌────────────────────────────────────────┐
│ OAuth2LoginSuccessHandler.initializeRoutes() │
│   ↓                                    │
│   providersOptional.get()              │ ← 此时获取是安全的
│   ↓                                    │
│   provider.getHandler()                │
│   ↓                                    │
│   handlerProvider.getObject()          │ ← 所有 Bean 已存在，可以安全获取
└────────────────────────────────────────┘
         ↓
阶段 3: SecurityFilterChain 配置
┌────────────────────────────────────────┐
│ WebSecurityConfig.securityFilterChain() │
│   ↓                                    │
│   使用 oauth2LoginSuccessHandler       │ ← 此时 Handler 已完全初始化
└────────────────────────────────────────┘
```

### 方案 2: 使用 @Lazy 注解（备选）

如果不想使用 `@PostConstruct`，也可以使用 `@Lazy` 注解：

```java
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final Lazy<Map<String, AuthenticationSuccessHandler>> lazyHandlerMap;
    
    public OAuth2LoginSuccessHandler(
            @Lazy Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional) {
        this.lazyHandlerMap = Lazy.of(() -> buildHandlerMap(providersOptional));
    }
    
    private Map<String, AuthenticationSuccessHandler> buildHandlerMap(
            Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional) {
        // 构建路由表
        // ...
    }
    
    @Override
    public void onAuthenticationSuccess(...) {
        Map<String, AuthenticationSuccessHandler> handlerMap = lazyHandlerMap.get();
        // 使用路由表
    }
}
```

### 方案 3: 允许循环引用（不推荐）

```yaml
spring:
  main:
    allow-circular-references: true
```

**不推荐原因**:
- ❌ 掩盖了设计问题
- ❌ Spring Boot 3.x 默认禁用，有其原因
- ❌ 可能导致不可预测的行为
- ❌ 违反最佳实践

## 📊 方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| @PostConstruct | ✅ 清晰明确<br>✅ 符合 Spring 最佳实践<br>✅ 初始化时机可控 | 需要额外的方法 | ⭐⭐⭐⭐⭐ |
| @Lazy | ✅ 自动延迟加载<br>✅ 代码简洁 | 首次使用时才初始化<br>可能影响性能 | ⭐⭐⭐⭐ |
| allow-circular-references | 快速解决 | ❌ 掩盖问题<br>❌ 违反最佳实践 | ⭐ |

## 🎯 最终方案

采用 **@PostConstruct + ObjectProvider** 组合方案：

1. ✅ `OAuth2LoginSuccessHandler` 使用 `@PostConstruct` 延迟初始化路由表
2. ✅ `SystemOAuth2LoginSuccessHandlerProvider` 使用 `ObjectProvider` 延迟获取 Handler
3. ✅ 构造函数只保存引用，不执行复杂逻辑
4. ✅ 所有初始化操作在 `@PostConstruct` 阶段执行

### 优势

- ✅ **无循环依赖**: Spring 可以正常创建所有 Bean
- ✅ **清晰明确**: 初始化时机和顺序清晰
- ✅ **符合最佳实践**: 遵循 Spring 推荐的模式
- ✅ **易于调试**: 日志清楚显示初始化过程
- ✅ **性能良好**: 只在应用启动时初始化一次

## 📝 检查清单

在实现 SPI 架构时，避免循环依赖的检查清单：

- [ ] 构造函数只保存依赖引用，不执行复杂逻辑
- [ ] 使用 `@PostConstruct` 进行延迟初始化
- [ ] 使用 `ObjectProvider<T>` 而非直接注入
- [ ] 使用 `Optional<List<T>>` 处理可选的 SPI 实现
- [ ] 避免在构造函数中调用 `Optional.get()`
- [ ] 日志记录初始化过程，便于调试

## 🔧 验证方法

### 1. 启动应用查看日志

正常情况下应该看到：

```
OAuth2LoginSuccessHandler 构造函数执行（延迟初始化路由表）
SystemOAuth2LoginSuccessHandlerProvider 构造函数执行
SystemOAuth2LoginSuccessHandler 构造函数执行
...
OAuth2LoginSuccessHandler 初始化路由表（@PostConstruct）
注册路由: logto → SystemOAuth2LoginSuccessHandler (优先级: 100)
注册路由: logto-admin → SystemOAuth2LoginSuccessHandler (优先级: 100)
设置默认 Handler: SystemOAuth2LoginSuccessHandler
OAuth2LoginSuccessHandler 路由表初始化完成: 已注册 2 个路由规则
```

### 2. 如果仍有循环依赖

检查以下几点：

1. 确认 `OAuth2LoginSuccessHandler` 的构造函数中没有调用 `providersOptional.get()`
2. 确认路由表初始化在 `@PostConstruct` 方法中
3. 确认 Provider 使用了 `ObjectProvider<T>`
4. 检查是否有其他隐藏的依赖链

## 📚 相关文档

- `oauth2-handler-spi-architecture.md` - OAuth2 Handler SPI 架构设计
- `oauth2-handler-trigger-flow.md` - OAuth2 Handler 触发流程详解

---

**创建时间**: 2025-10-04  
**问题状态**: ✅ 已解决  
**解决方案**: @PostConstruct + ObjectProvider
