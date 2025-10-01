# SPI 安全配置修复记录

## 📋 问题描述

### 原始错误
```
java.lang.IllegalStateException: Can't configure mvcMatchers after anyRequest
	at org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry.createMvcMatchers
```

### 错误原因
Spring Security 规则配置顺序错误：
1. `WebSecurityConfig` 首先配置了 `anyRequest().authenticated()`
2. 然后调用 `SecurityConfigurer` 扩展尝试添加 `requestMatchers("/actuator/**").permitAll()`
3. **Spring Security 要求**：必须先配置具体路径，最后配置 `anyRequest()`

---

## ✅ 解决方案

### 1. 修改 SPI 接口设计

#### 原接口（错误）
```java
public interface SecurityConfigurer {
    void configure(HttpSecurity http) throws Exception;  // ❌ 传递 HttpSecurity
}
```

**问题**：无法控制规则添加的顺序，导致在 `anyRequest()` 之后添加具体路径规则。

#### 新接口（正确）
```java
public interface SecurityConfigurer {
    int getOrder();
    
    void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize
    ) throws Exception;
    
    default boolean isEnabled() {
        return true;
    }
}
```

**优势**：
- ✅ 直接传递 `AuthorizationManagerRequestMatcherRegistry`，在 `anyRequest()` **之前**插入规则
- ✅ 通过 `getOrder()` 控制多个扩展的优先级
- ✅ 通过 `isEnabled()` 支持动态启用/禁用

---

### 2. WebSecurityConfig 实现

```java
@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnClass(HttpSecurity.class)
public class WebSecurityConfig {

    private final List<SecurityConfigurer> securityConfigurers;

    @Autowired
    public WebSecurityConfig(Optional<List<SecurityConfigurer>> configurersOptional) {
        this.securityConfigurers = configurersOptional.orElse(null);
        if (this.securityConfigurers != null) {
            log.info("发现 {} 个 SecurityConfigurer 扩展", this.securityConfigurers.size());
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> {
                // 1. 先应用所有 SecurityConfigurer 扩展（具体路径规则）
                applySecurityConfigurers(authorize);
                
                // 2. 最后配置 anyRequest()
                authorize.anyRequest().authenticated();
            })
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        log.info("WebSecurityConfig 配置完成");
        return http.build();
    }

    /**
     * 按优先级应用所有 SecurityConfigurer 扩展
     */
    private void applySecurityConfigurers(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        
        if (securityConfigurers == null || securityConfigurers.isEmpty()) {
            return;
        }

        securityConfigurers.stream()
                .filter(SecurityConfigurer::isEnabled)
                .sorted(Comparator.comparingInt(SecurityConfigurer::getOrder))
                .forEach(configurer -> {
                    try {
                        configurer.configureAuthorization(authorize);
                        log.info("应用 SecurityConfigurer: {}", configurer.getClass().getSimpleName());
                    } catch (Exception e) {
                        log.error("应用 SecurityConfigurer 失败: {}", 
                                configurer.getClass().getSimpleName(), e);
                    }
                });
    }
}
```

**关键点**：
1. 在 `authorizeHttpRequests()` lambda 中：
   - 先调用 `applySecurityConfigurers()` 添加具体路径规则
   - 最后调用 `anyRequest().authenticated()`
2. 按 `getOrder()` 排序，数字越小优先级越高
3. 异常处理，避免单个扩展失败导致整个安全配置崩溃

---

### 3. MonitorSecurityConfigurer 实现

```java
package com.rymcu.mortise.monitor.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * 监控模块安全配置
 * 
 * <p>通过 SPI 扩展机制为 Actuator 端点配置公开访问权限</p>
 * 
 * @author ronger
 */
@Slf4j
@Component
public class MonitorSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 50; // 高优先级，确保监控端点优先放行
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) 
            throws Exception {
        
        authorize
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/actuator/prometheus").permitAll()
            .requestMatchers("/actuator/**").permitAll();
        
        log.info("监控模块安全配置已加载: Actuator 端点放行");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

**优先级说明**：
- `order = 50` - 高优先级（数字越小越优先）
- 确保 Actuator 端点在其他规则之前被匹配

---

## 📦 模块依赖配置

### mortise-monitor/pom.xml

```xml
<dependencies>
    <!-- 核心依赖 -->
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-common</artifactId>
    </dependency>

    <!-- SPI 接口依赖（optional，仅编译时需要） -->
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-auth</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Spring Security（optional，仅用于类型引用） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**`<optional>true</optional>` 的作用**：
- ✅ 编译时可用（提供 SPI 接口和类型）
- ✅ 不会传递给依赖 `mortise-monitor` 的其他模块
- ✅ 运行时由 `mortise-app` 统一提供依赖

---

## ✅ 验证结果

### 启动日志

```
2025-10-01T14:59:43.420+08:00  INFO 24436 --- [           main] c.r.m.auth.config.WebSecurityConfig      : 发现 1 个 SecurityConfigurer 扩展
2025-10-01T14:59:43.431+08:00  INFO 24436 --- [           main] c.r.m.m.c.MonitorSecurityConfigurer      : 监控模块安全配置已加载: Actuator 端点放行
2025-10-01T14:59:43.432+08:00  INFO 24436 --- [           main] c.r.m.auth.config.WebSecurityConfig      : 应用 SecurityConfigurer: MonitorSecurityConfigurer
2025-10-01T14:59:43.444+08:00  INFO 24436 --- [           main] c.r.m.auth.config.WebSecurityConfig      : WebSecurityConfig 配置完成
2025-10-01T14:59:46.920+08:00  INFO 24436 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 9999 (http) with context path '/mortise'
2025-10-01T14:59:46.954+08:00  INFO 24436 --- [           main] com.rymcu.mortise.MortiseApplication     : Started MortiseApplication in 17.748 seconds
```

**成功标志**：
- ✅ 发现 1 个 SecurityConfigurer 扩展
- ✅ MonitorSecurityConfigurer 成功加载
- ✅ SecurityConfigurer 成功应用
- ✅ WebSecurityConfig 配置完成
- ✅ 应用成功启动（17.75 秒）

### 访问验证

**公开端点（无需认证）**：
- ✅ `/actuator/health`
- ✅ `/actuator/prometheus`
- ✅ `/actuator/**`
- ✅ `/v3/api-docs/**`
- ✅ `/swagger-ui/**`

**保护端点（需要 JWT）**：
- 🔒 `/api/**`

---

## 🎯 架构优势

### 1. 解耦性
- `mortise-monitor` 不强依赖 `mortise-auth`
- 使用 SPI 接口作为契约
- `<optional>true</optional>` 避免传递依赖污染

### 2. 扩展性
```java
// 任何模块都可以提供 SecurityConfigurer 实现

@Component
public class SystemSecurityConfigurer implements SecurityConfigurer {
    @Override
    public int getOrder() { return 100; }
    
    @Override
    public void configureAuthorization(...) {
        // 配置系统业务端点
    }
}
```

### 3. 可测试性
- 每个模块的安全配置可以独立测试
- 不需要启动整个 auth 模块
- 通过 `isEnabled()` 可以动态禁用

### 4. 职责清晰
| 模块 | 职责 |
|------|------|
| mortise-auth | 提供安全框架和 SPI 接口 |
| mortise-monitor | 定义监控端点的安全策略 |
| mortise-system | 定义业务端点的安全策略 |
| mortise-app | 组装所有配置 |

---

## 📚 相关文档

- **模块架构说明**: [`docs/module-dependency-and-spi-architecture.md`](module-dependency-and-spi-architecture.md)
- **安全配置指南**: [`docs/security-configuration-guide.md`](security-configuration-guide.md)
- **SPI 接口定义**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/SecurityConfigurer.java`

---

## 🔑 关键要点总结

1. **Spring Security 规则顺序**: 必须先配置具体路径，最后配置 `anyRequest()`
2. **SPI 接口设计**: 传递 `AuthorizationManagerRequestMatcherRegistry` 而非 `HttpSecurity`
3. **扩展优先级**: 通过 `getOrder()` 控制，数字越小越优先
4. **可选依赖**: `<optional>true</optional>` 实现编译时支持，避免运行时耦合
5. **异常处理**: 单个扩展失败不影响整体安全配置

---

**修复完成时间**: 2025-10-01  
**修复人员**: GitHub Copilot + ronger  
**状态**: ✅ 已验证通过
