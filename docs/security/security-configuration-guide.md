# Security 配置指南

## 问题说明

### 问题 1: Actuator 被重定向到 OAuth2 登录页

**原因**: 多模块重构后缺少 `WebSecurityConfig` 配置类，Spring Security 使用默认配置，要求所有路径都需要认证。

**解决方案**: 已在 `mortise-auth` 模块中创建 `WebSecurityConfig.java`，配置了无需认证的端点。

---

## 无需认证的端点配置

`mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java` 中已配置以下端点无需认证：

### 1. 健康检查和监控
```java
authorize.requestMatchers("/actuator/**").permitAll();
```

**访问示例**:
- `http://localhost:9999/mortise/actuator`
- `http://localhost:9999/mortise/actuator/health`
- `http://localhost:9999/mortise/actuator/metrics`
- `http://localhost:9999/mortise/actuator/prometheus`

### 2. OpenAPI 3.0 文档
```java
authorize.requestMatchers("/v3/api-docs/**").permitAll();
authorize.requestMatchers("/swagger-ui/**").permitAll();
authorize.requestMatchers("/swagger-ui.html").permitAll();
```

**访问示例**:
- `http://localhost:9999/mortise/swagger-ui.html` - Swagger UI 界面
- `http://localhost:9999/mortise/swagger-ui/index.html` - Swagger UI 首页
- `http://localhost:9999/mortise/v3/api-docs` - OpenAPI JSON 文档
- `http://localhost:9999/mortise/v3/api-docs/swagger-config` - Swagger 配置

### 3. 静态资源
```java
authorize.requestMatchers("/static/**").permitAll();
authorize.requestMatchers("/webjars/**").permitAll();
```

### 4. 认证相关 API
```java
authorize.requestMatchers("/api/v1/auth/login").permitAll();
authorize.requestMatchers("/api/v1/auth/register").permitAll();
authorize.requestMatchers("/api/v1/auth/logout").permitAll();
authorize.requestMatchers("/api/v1/auth/refresh-token").permitAll();
```

### 5. CORS 预检请求
```java
authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
```

---

## 如何添加自定义的无需认证端点

### 方式 1: 直接修改 WebSecurityConfig

在 `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java` 中添加：

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> {
            // ... 现有配置 ...
            
            // 添加你的端点
            authorize.requestMatchers("/your-endpoint/**").permitAll();
            
            authorize.anyRequest().authenticated();
        });
    
    return http.build();
}
```

### 方式 2: 使用 SecurityConfigurer SPI（推荐）

创建一个实现 `SecurityConfigurer` 接口的配置类：

```java
package com.rymcu.mortise.system.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/**
 * 系统模块的安全配置扩展
 */
@Component
public class SystemSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 200; // 优先级（数字越小优先级越高）
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> 
            authorize
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/system/info").permitAll()
        );
    }
}
```

**优势**:
- ✅ 模块化：每个业务模块管理自己的安全配置
- ✅ 解耦：无需修改 auth 模块的核心配置
- ✅ 可测试：独立的配置类更易于测试

---

## 配置验证

### 启动应用后验证

1. **验证 Actuator 无需认证**:
```bash
curl http://localhost:9999/mortise/actuator
curl http://localhost:9999/mortise/actuator/health
```

2. **验证 Swagger UI 无需认证**:
```bash
# 浏览器访问
http://localhost:9999/mortise/swagger-ui.html
```

3. **验证 API 文档无需认证**:
```bash
curl http://localhost:9999/mortise/v3/api-docs
```

### 查看应用日志

启动时会看到以下日志：

```
INFO  c.r.m.a.config.WebSecurityConfig : 发现 X 个 SecurityConfigurer 扩展
INFO  c.r.m.a.config.WebSecurityConfig : 应用 SecurityConfigurer: XxxSecurityConfigurer
INFO  c.r.m.a.config.WebSecurityConfig : WebSecurityConfig 配置完成
```

---

## 常见问题

### Q1: 为什么还是被重定向？

**检查清单**:
1. 确认 `WebSecurityConfig` 类被 Spring 扫描到
2. 确认 `@EnableWebSecurity` 注解存在
3. 确认端点路径匹配（注意 context-path: `/mortise`）
4. 查看日志确认配置已加载

### Q2: 如何配置需要特定角色才能访问的端点？

```java
authorize.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
authorize.requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN");
```

### Q3: 如何配置 OAuth2 登录？

需要在 `SecurityConfigurer` SPI 实现中配置：

```java
@Override
public void configure(HttpSecurity http) throws Exception {
    http.oauth2Login(oauth2 -> oauth2
        .loginPage("/oauth2/authorization/your-provider")
        .defaultSuccessUrl("/home")
    );
}
```

---

## 相关文件

- **核心配置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java`
- **SPI 接口**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/SecurityConfigurer.java`
- **JWT 过滤器**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/filter/JwtAuthenticationFilter.java`

---

## 总结

✅ **已解决问题**:
1. Actuator 端点现在无需认证即可访问
2. OpenAPI/Swagger 文档无需认证即可访问
3. 提供了灵活的 SPI 扩展机制

✅ **最佳实践**:
- 使用 `SecurityConfigurer` SPI 扩展安全配置
- 业务模块独立管理自己的安全策略
- 核心 auth 模块提供基础配置
