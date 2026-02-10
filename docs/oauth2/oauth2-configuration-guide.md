# OAuth2 配置完善指南

## 问题背景

1. **需要添加 OAuth2 相关的 Bean 配置吗？**
   - 是的，需要添加
   
2. **如何在业务层监听 `oidcUserService` 中发布的事件？**
   - 使用 Spring 的 `@EventListener` 注解

## 一、已创建的文件

### 1. OAuth2LoginSuccessHandler
**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LoginSuccessHandler.java`

**作用**: 处理 OAuth2 登录成功后的逻辑

```java
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 处理 OAuth2 登录成功
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();
            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                // 重定向到回调页面
                String redirectUrl = "/api/v1/auth/oauth2/callback?registrationId=" + registrationId;
                response.sendRedirect(redirectUrl);
            }
        }
    }
}
```

### 2. OAuth2LogoutSuccessHandler
**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LogoutSuccessHandler.java`

**作用**: 处理 OAuth2 登出成功后的逻辑

```java
@Slf4j
public class OAuth2LogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Authentication authentication) {
        // 处理登出逻辑
    }
}
```

### 3. RewriteAccessDeniedHandler
**位置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/RewriteAccessDeniedHandler.java`

**作用**: 处理访问被拒绝的情况（403 错误）

```java
@Slf4j
@Component
public class RewriteAccessDeniedHandler implements AccessDeniedHandler {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        // 返回统一的 JSON 错误响应
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        GlobalResult<Object> result = GlobalResult.error(ResultCode.UNAUTHORIZED);
        outputStream.write(objectMapper.writeValueAsBytes(result));
    }
}
```

## 二、WebSecurityConfig 需要添加的配置

### 完整的 Bean 配置

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Resource
    private RewriteAccessDeniedHandler rewriteAccessDeniedHandler;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public WebSecurityConfig(
            Optional<ClientRegistrationRepository> clientRegistrationRepositoryOptional) {
        this.clientRegistrationRepository = clientRegistrationRepositoryOptional.orElse(null);
    }

    // 1. 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 认证管理器
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 3. OAuth2 登录成功处理器
    @Bean
    public OAuth2LoginSuccessHandler oauth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler();
    }

    // 4. OAuth2 登出成功处理器
    @Bean
    public OAuth2LogoutSuccessHandler oauth2LogoutSuccessHandler() {
        return new OAuth2LogoutSuccessHandler();
    }

    // 5. OIDC 用户服务 - 关键！在这里发布事件
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();
        return request -> {
            // 获取用户信息
            OidcUser user = delegate.loadUser(request);
            
            // 发布事件 - 业务层可以监听此事件
            applicationEventPublisher.publishEvent(new OidcUserEvent(user));
            
            return user;
        };
    }

    // 6. OAuth2 授权请求解析器
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        if (clientRegistrationRepository == null) {
            return null;
        }
        
        DefaultOAuth2AuthorizationRequestResolver resolver = 
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, 
                        "/api/v1/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(authorizationRequestCustomizer());
        return resolver;
    }

    // 7. OAuth2 授权请求自定义器
    private Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer() {
        return customizer -> customizer
                .additionalParameters(params -> params.put("prompt", "consent"));
    }

    // 8. Security 过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> {
                // 配置公开端点
                authorize.requestMatchers("/api/v1/auth/**").permitAll();
                authorize.anyRequest().authenticated();
            })
            .httpBasic(Customizer.withDefaults());

        // 配置异常处理
        http.exceptionHandling(exception -> 
            exception.accessDeniedHandler(rewriteAccessDeniedHandler)
        );

        // 配置 OAuth2 登录
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2Login -> 
                oauth2Login
                    .authorizationEndpoint(authorization -> {
                        OAuth2AuthorizationRequestResolver resolver = authorizationRequestResolver();
                        if (resolver != null) {
                            authorization.authorizationRequestResolver(resolver);
                        }
                    })
                    .redirectionEndpoint(redirection -> 
                        redirection.baseUri("/api/v1/oauth2/code/*"))
                    .userInfoEndpoint(userInfoEndpoint -> 
                        userInfoEndpoint.oidcUserService(oidcUserService()))
                    .successHandler(oauth2LoginSuccessHandler())
            );
            
            // 配置登出
            http.logout(logout -> 
                logout.logoutSuccessHandler(oauth2LogoutSuccessHandler())
            );
        }

        // 添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## 三、如何在业务层监听 OidcUserEvent

### 方法1：使用 @EventListener（推荐）

项目中已经有一个监听器示例：
**位置**: `mortise-system/src/main/java/com/rymcu/mortise/system/handler/OidcUserEventHandler.java`

```java
@Slf4j
@Component
public class OidcUserEventHandler {

    @Resource
    private UserService userService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 监听 OIDC 用户事件
     * 
     * @Async 表示异步处理，不阻塞主流程
     * @EventListener 表示监听 OidcUserEvent 事件
     */
    @Async
    @EventListener
    public void processOidcUserEvent(OidcUserEvent oidcUserEvent) throws JsonProcessingException {
        OidcUser user = oidcUserEvent.getUser();
        
        log.info("收到 OIDC 用户登录事件: {}", user.getEmail());
        
        // 在这里添加业务逻辑
        // 1. 保存用户到数据库
        // 2. 更新用户信息
        // 3. 记录登录日志
        // 4. 发送欢迎邮件
        // 5. 更新最后在线时间
        
        // 示例：打印用户信息
        System.out.println(objectMapper.writeValueAsString(user));
        
        // 示例：保存到数据库
        saveOrUpdateUser(user);
    }
    
    /**
     * 保存或更新用户到数据库
     */
    private void saveOrUpdateUser(OidcUser oidcUser) {
        // 查找用户
        User existingUser = userService.findByEmail(oidcUser.getEmail());
        
        if (existingUser == null) {
            // 创建新用户
            User newUser = new User();
            newUser.setEmail(oidcUser.getEmail());
            newUser.setNickname(oidcUser.getName());
            newUser.setAvatar(oidcUser.getPicture());
            newUser.setOpenId(oidcUser.getSubject());
            userService.save(newUser);
            log.info("创建新用户: {}", newUser.getEmail());
        } else {
            // 更新用户信息
            existingUser.setNickname(oidcUser.getName());
            existingUser.setAvatar(oidcUser.getPicture());
            userService.updateById(existingUser);
            log.info("更新用户信息: {}", existingUser.getEmail());
        }
    }
}
```

### 方法2：使用 @TransactionalEventListener

如果需要在事务中处理事件：

```java
@Component
public class OidcUserTransactionalHandler {

    @Resource
    private UserService userService;

    /**
     * 在事务提交后处理事件
     * 
     * phase = TransactionPhase.AFTER_COMMIT 表示事务提交后执行
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOidcUserEvent(OidcUserEvent event) {
        OidcUser user = event.getUser();
        
        // 在这里处理数据库操作
        // 此时事务已经提交，可以安全地进行异步操作
    }
}
```

### 方法3：直接在 AuthService 中处理

参考 `AuthServiceImpl.oauth2Login` 方法：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public TokenUser oauth2Login(OidcUser oidcUser, String registrationId) {
    // 1. 查找或创建用户
    User user = findOrCreateUser(oidcUser, registrationId);

    // 2. 为用户生成并存储令牌
    return generateAndStoreTokens(user);
}

// 核心逻辑：查找或创建用户
@Transactional(rollbackFor = Exception.class)
protected User findOrCreateUser(OidcUser oidcUser, String registrationId) {
    User newUser = createNewUser(oidcUser, registrationId);

    try {
        String code = Utils.genPassword();
        newUser.setPassword(passwordEncoder.encode(code));
        boolean saved = userService.save(newUser);
        if (saved) {
            // 发布注册事件
            applicationEventPublisher.publishEvent(
                new RegisterEvent(newUser.getId(), newUser.getEmail(), code));
            return newUser;
        }
    } catch (DataIntegrityViolationException e) {
        // 用户已存在，查询并更新
        User existingUser = userService.getMapper().selectOneByQuery(
                QueryWrapper.create()
                        .where(User::getProvider).eq(registrationId)
                        .and(User::getOpenId).eq(oidcUser.getSubject())
        );
        if (existingUser != null) {
            return updateExistingUser(existingUser, oidcUser);
        }
    }

    throw new BusinessException(ResultCode.REGISTER_FAIL.getMessage());
}
```

## 四、事件处理的最佳实践

### 1. 使用 @Async 异步处理

```java
@Async
@EventListener
public void handleEvent(OidcUserEvent event) {
    // 异步处理，不阻塞主流程
}
```

### 2. 添加异常处理

```java
@EventListener
public void handleEvent(OidcUserEvent event) {
    try {
        // 处理逻辑
    } catch (Exception e) {
        log.error("处理 OIDC 用户事件失败", e);
        // 不要抛出异常，避免影响主流程
    }
}
```

### 3. 使用事务

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleEvent(OidcUserEvent event) {
    // 事务提交后执行
}
```

### 4. 添加条件判断

```java
@EventListener(condition = "#event.user.email != null")
public void handleEvent(OidcUserEvent event) {
    // 只有当 email 不为 null 时才处理
}
```

## 五、完整的事件流程

```
1. 用户发起 OAuth2 登录
   ↓
2. Spring Security 重定向到 OAuth2 提供商
   ↓
3. 用户授权后重定向回应用
   ↓
4. oidcUserService 获取用户信息
   ↓
5. 发布 OidcUserEvent 事件
   ↓
6. OidcUserEventHandler 监听并处理事件
   ├── 保存用户到数据库
   ├── 更新用户信息
   ├── 记录登录日志
   └── 其他业务逻辑
   ↓
7. OAuth2LoginSuccessHandler 处理登录成功
   ↓
8. 重定向到前端回调页面
```

## 六、注意事项

1. **避免循环依赖**：`mortise-auth` 不能直接依赖 `mortise-system`，使用事件解耦
2. **异步处理**：使用 `@Async` 避免阻塞主流程
3. **异常处理**：事件处理器中要捕获异常，避免影响主流程
4. **事务管理**：根据需要选择合适的事务传播行为
5. **幂等性**：确保事件处理是幂等的，避免重复处理

## 七、相关文件

- `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java` - 安全配置
- `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LoginSuccessHandler.java` - 登录成功处理器
- `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/OAuth2LogoutSuccessHandler.java` - 登出成功处理器
- `mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/RewriteAccessDeniedHandler.java` - 访问拒绝处理器
- `mortise-system/src/main/java/com/rymcu/mortise/system/handler/OidcUserEventHandler.java` - OIDC 用户事件处理器
- `mortise-system/src/main/java/com/rymcu/mortise/system/handler/event/OidcUserEvent.java` - OIDC 用户事件
- `mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/AuthServiceImpl.java` - 认证服务实现

## 日期

2025-10-01
