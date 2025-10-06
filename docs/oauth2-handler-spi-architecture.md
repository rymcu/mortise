# OAuth2 登录处理器 SPI 架构设计

## 📋 设计目标

实现一个灵活、可扩展的 OAuth2 登录处理器架构，支持：
1. ✅ 多模块动态注册（System、Member）
2. ✅ 多 Provider 支持（Logto、GitHub、Google、微信等）
3. ✅ 同一 Provider 不同配置（logto-admin vs logto-member）
4. ✅ 避免循环依赖
5. ✅ 使用 Optional 而非 `@Autowired(required = false)`

## 🏗️ 架构设计

### 1. SPI 接口定义

**文件**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/OAuth2LoginSuccessHandlerProvider.java`

```java
public interface OAuth2LoginSuccessHandlerProvider {
    
    /**
     * 获取处理器（延迟加载，避免循环依赖）
     */
    AuthenticationSuccessHandler getHandler();
    
    /**
     * 支持的 registrationId 列表
     * 示例: ["logto", "logto-admin"]
     */
    String[] getSupportedRegistrationIds();
    
    /**
     * 是否为默认处理器
     * 当找不到匹配的 registrationId 时使用
     */
    default boolean isDefault() {
        return false;
    }
    
    /**
     * 优先级（数字越大优先级越高）
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
}
```

### 2. 核心路由器

**文件**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LoginSuccessHandler.java`

**重构要点**:
- ✅ 合并了原 `OAuth2LoginSuccessHandlerRouter` 的功能
- ✅ 使用 `Optional<List<OAuth2LoginSuccessHandlerProvider>>` 注入
- ✅ 基于 SPI 自动发现所有 Provider
- ✅ 按优先级排序路由表
- ✅ 支持默认 Handler
- ✅ 使用 `@PostConstruct` 延迟初始化，避免循环依赖

```java
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final Map<String, AuthenticationSuccessHandler> handlerMap = new HashMap<>();
    private AuthenticationSuccessHandler defaultHandler;
    private final Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional;
    
    /**
     * 构造函数只保存 Provider 引用，不立即初始化路由表
     */
    public OAuth2LoginSuccessHandler(
            Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional) {
        this.providersOptional = providersOptional;
        log.info("OAuth2LoginSuccessHandler 构造函数执行（延迟初始化路由表）");
    }
    
    /**
     * 使用 @PostConstruct 在所有依赖注入完成后初始化路由表
     * 这样可以避免循环依赖问题
     */
    @PostConstruct
    public void initializeRoutes() {
        if (providersOptional.isEmpty()) {
            log.warn("未发现任何 OAuth2LoginSuccessHandlerProvider");
            return;
        }
        
        List<OAuth2LoginSuccessHandlerProvider> providers = providersOptional.get();
        
        // 按优先级排序并注册路由
        providers.stream()
            .filter(OAuth2LoginSuccessHandlerProvider::isEnabled)
            .sorted(Comparator.comparingInt(
                OAuth2LoginSuccessHandlerProvider::getOrder).reversed())
            .forEach(this::registerProvider);
    }
    
    @Override
    public void onAuthenticationSuccess(...) {
        String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();
        
        // 路由到对应的 Handler
        AuthenticationSuccessHandler handler = 
            handlerMap.getOrDefault(registrationId, defaultHandler);
        
        handler.onAuthenticationSuccess(request, response, authentication);
    }
}
```

### 3. System 模块实现

**文件**: `mortise-system/src/main/java/com/rymcu/mortise/system/auth/SystemOAuth2LoginSuccessHandlerProvider.java`

**关键改进**:
- ✅ 使用 `ObjectProvider<T>` 延迟加载，避免循环依赖
- ✅ 在 `getHandler()` 方法中才真正获取 Bean 实例

```java
@Component
public class SystemOAuth2LoginSuccessHandlerProvider 
        implements OAuth2LoginSuccessHandlerProvider {
    
    private final ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider;
    
    /**
     * 构造函数注入 ObjectProvider（延迟加载）
     */
    public SystemOAuth2LoginSuccessHandlerProvider(
            ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }
    
    @Override
    public AuthenticationSuccessHandler getHandler() {
        // 延迟获取 Handler 实例，避免循环依赖
        return handlerProvider.getObject();
    }
    
    @Override
    public String[] getSupportedRegistrationIds() {
        return new String[] { "logto", "logto-admin" };
    }
    
    @Override
    public boolean isDefault() {
        return true;  // 系统端作为默认处理器
    }
    
    @Override
    public int getOrder() {
        return 100;  // 高优先级
    }
}
```

### 4. Member 模块实现

**文件**: `mortise-member/src/main/java/com/rymcu/mortise/member/auth/MemberOAuth2LoginSuccessHandlerProvider.java`

```java
@Component
public class MemberOAuth2LoginSuccessHandlerProvider 
        implements OAuth2LoginSuccessHandlerProvider {
    
    private final ObjectProvider<MemberOAuth2LoginSuccessHandler> handlerProvider;
    
    public MemberOAuth2LoginSuccessHandlerProvider(
            ObjectProvider<MemberOAuth2LoginSuccessHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }
    
    @Override
    public AuthenticationSuccessHandler getHandler() {
        return handlerProvider.getObject();
    }
    
    @Override
    public String[] getSupportedRegistrationIds() {
        return new String[] { 
            "logto-member", "github", "google", "wechat" 
        };
    }
    
    @Override
    public int getOrder() {
        return 50;  // 普通优先级
    }
}
```

### 5. WebSecurityConfig 配置

**文件**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java`

```java
@Configuration
public class WebSecurityConfig {
    
    @Resource
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // ...
        
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2Login ->
                oauth2Login
                    // ... 其他配置 ...
                    .successHandler(oauth2LoginSuccessHandler)  // 使用基于 SPI 的处理器
            );
        }
        
        return http.build();
    }
}
```

## 🔄 完整的 Bean 初始化流程

### 正常流程（无循环依赖）

```
1. WebSecurityConfig 初始化
   ↓
2. OAuth2LoginSuccessHandler 初始化（构造函数）
   - 注入 Optional<List<OAuth2LoginSuccessHandlerProvider>>
   - ⚠️ 此时不获取 Provider 列表，只保存引用
   - ⚠️ 不构建路由表
   ↓
3. SystemOAuth2LoginSuccessHandlerProvider 初始化
   - 注入 ObjectProvider<SystemOAuth2LoginSuccessHandler>
   - 此时不获取 Handler 实例，只保存 Provider
   ↓
4. SystemOAuth2LoginSuccessHandler 初始化
   - 注入 AuthService 等依赖
   ↓
5. AuthServiceImpl 初始化
   ↓
6. UserServiceImpl 初始化
   ↓
7. 所有 Bean 初始化完成
   ↓
8. Spring 调用 @PostConstruct 方法
   - OAuth2LoginSuccessHandler.initializeRoutes()
   - 此时获取 Provider 列表
   - 调用 provider.getHandler()
   - handlerProvider.getObject() ← 此时才真正获取 Handler 实例
   - 构建路由表
   ↓
9. 第一次 OAuth2 登录时
   - OAuth2LoginSuccessHandler.onAuthenticationSuccess()
   - 从路由表查找对应的 Handler
   - 委托给具体的 Handler 处理
```

### 关键技术点

1. **Optional 替代 @Autowired(required = false)**
   ```java
   // ❌ 不推荐
   @Autowired(required = false)
   private List<OAuth2LoginSuccessHandlerProvider> providers;
   
   // ✅ 推荐
   public OAuth2LoginSuccessHandler(
       Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional)
   ```

2. **@PostConstruct 延迟初始化路由表（避免循环依赖）**
   ```java
   @Component
   public class OAuth2LoginSuccessHandler {
       private final Optional<List<...>> providersOptional;
       
       // 构造函数只保存引用
       public OAuth2LoginSuccessHandler(Optional<List<...>> providersOptional) {
           this.providersOptional = providersOptional;
       }
       
       // 在所有依赖注入完成后初始化
       @PostConstruct
       public void initializeRoutes() {
           List<...> providers = providersOptional.get();
           // 构建路由表
       }
   }
   ```

3. **ObjectProvider 延迟获取 Handler（避免循环依赖）**
   ```java
   // ❌ 直接注入会导致循环依赖
   @Resource
   private SystemOAuth2LoginSuccessHandler handler;
   
   // ✅ 使用 ObjectProvider 延迟获取
   private final ObjectProvider<SystemOAuth2LoginSuccessHandler> handlerProvider;
   
   @Override
   public AuthenticationSuccessHandler getHandler() {
       return handlerProvider.getObject();  // 延迟加载
   }
   ```

4. **SPI 自动发现**
   - Spring 自动扫描所有实现 `OAuth2LoginSuccessHandlerProvider` 的 Bean
   - 无需手动配置路由规则
   - 各模块独立管理自己的 Provider

## 📊 路由表示例

| registrationId | Handler | 优先级 | 模块 |
|----------------|---------|--------|------|
| logto | SystemOAuth2LoginSuccessHandler | 100 | system |
| logto-admin | SystemOAuth2LoginSuccessHandler | 100 | system |
| logto-member | MemberOAuth2LoginSuccessHandler | 50 | member |
| github | MemberOAuth2LoginSuccessHandler | 50 | member |
| google | MemberOAuth2LoginSuccessHandler | 50 | member |
| wechat | MemberOAuth2LoginSuccessHandler | 50 | member |
| **(default)** | SystemOAuth2LoginSuccessHandler | - | system |

## 🎯 使用方式

### 配置 application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # 系统管理端 - Logto
          logto-admin:
            client-id: ${LOGTO_ADMIN_CLIENT_ID}
            client-secret: ${LOGTO_ADMIN_CLIENT_SECRET}
            redirect-uri: ${baseUrl}/api/v1/oauth2/code/logto-admin
            scope: openid,profile,email
            provider: logto
          
          # 用户端 - Logto
          logto-member:
            client-id: ${LOGTO_MEMBER_CLIENT_ID}
            client-secret: ${LOGTO_MEMBER_CLIENT_SECRET}
            redirect-uri: ${baseUrl}/api/v1/oauth2/code/logto-member
            scope: openid,profile,email
            provider: logto
          
          # 用户端 - GitHub
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            redirect-uri: ${baseUrl}/api/v1/oauth2/code/github
            scope: read:user,user:email
          
          # 用户端 - Google
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            redirect-uri: ${baseUrl}/api/v1/oauth2/code/google
            scope: openid,profile,email
        
        provider:
          logto:
            issuer-uri: https://auth.atdak.com/oidc
```

### 访问入口

- **系统管理端登录**: 
  - `/oauth2/authorization/logto-admin`
  - 路由到: `SystemOAuth2LoginSuccessHandler`
  
- **用户端登录**:
  - `/oauth2/authorization/logto-member` → `MemberOAuth2LoginSuccessHandler`
  - `/oauth2/authorization/github` → `MemberOAuth2LoginSuccessHandler`
  - `/oauth2/authorization/google` → `MemberOAuth2LoginSuccessHandler`

## 📝 扩展新的 Provider

如果需要添加新的 OAuth2 Provider（例如微博、QQ），只需：

1. **创建 Provider 实现**:

```java
@Component
public class WeiboOAuth2LoginSuccessHandlerProvider 
        implements OAuth2LoginSuccessHandlerProvider {
    
    private final ObjectProvider<WeiboOAuth2LoginSuccessHandler> handlerProvider;
    
    public WeiboOAuth2LoginSuccessHandlerProvider(
            ObjectProvider<WeiboOAuth2LoginSuccessHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }
    
    @Override
    public AuthenticationSuccessHandler getHandler() {
        return handlerProvider.getObject();
    }
    
    @Override
    public String[] getSupportedRegistrationIds() {
        return new String[] { "weibo" };
    }
    
    @Override
    public int getOrder() {
        return 30;
    }
}
```

2. **创建 Handler 实现**:

```java
@Component
public class WeiboOAuth2LoginSuccessHandler 
        implements AuthenticationSuccessHandler {
    // ... 实现登录逻辑
}
```

3. **配置 application.yml**:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          weibo:
            client-id: ${WEIBO_CLIENT_ID}
            client-secret: ${WEIBO_CLIENT_SECRET}
            # ... 其他配置
```

**无需修改任何其他代码**，SPI 机制会自动发现并注册！

## ✅ 优势总结

1. **解耦**: 各模块独立管理自己的 OAuth2 登录逻辑
2. **扩展性**: 轻松添加新的 Provider，无需修改核心代码
3. **灵活性**: 支持同一 Provider 不同配置
4. **可维护性**: 路由规则集中管理，易于调试
5. **无循环依赖**: 使用 ObjectProvider 延迟加载
6. **类型安全**: 使用 Optional 而非 required = false

## 🗑️ 已删除文件

- ✅ `OAuth2LoginSuccessHandlerRouter.java` - 功能已合并到 `OAuth2LoginSuccessHandler`

## 📚 相关文档

- `oauth2-handler-trigger-flow.md` - OAuth2 Handler 触发流程详解
- `oauth2-dual-logto-configuration.md` - 双 Logto 配置指南
- `oauth2-ultimate-simplification.md` - OAuth2 架构终极简化方案

---

**创建时间**: 2025-10-04  
**架构状态**: ✅ 已完成  
**循环依赖**: ✅ 已解决
