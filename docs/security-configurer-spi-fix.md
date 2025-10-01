# SecurityConfigurer SPI 架构修复说明

## 🐛 问题分析

### 错误信息
```
java.lang.IllegalStateException: Can't configure mvcMatchers after anyRequest
	at com.rymcu.mortise.monitor.config.MonitorSecurityConfigurer.configure(MonitorSecurityConfigurer.java:35)
```

### 根本原因

Spring Security 配置规则的**顺序约束**：

1. ❌ **错误顺序**:
   ```java
   http.authorizeHttpRequests(authorize -> {
       authorize.requestMatchers("/api/**").permitAll();
       authorize.anyRequest().authenticated(); // 先配置 anyRequest()
   });
   
   // 之后再配置具体路径会报错 ❌
   http.authorizeHttpRequests(authorize -> {
       authorize.requestMatchers("/actuator/**").permitAll(); // 💥 异常！
   });
   ```

2. ✅ **正确顺序**:
   ```java
   http.authorizeHttpRequests(authorize -> {
       authorize.requestMatchers("/api/**").permitAll();
       authorize.requestMatchers("/actuator/**").permitAll();
       // ... 所有具体路径配置
       authorize.anyRequest().authenticated(); // 最后配置 anyRequest()
   });
   ```

### 旧实现的问题

```java
// WebSecurityConfig.java (旧版)
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> {
        authorize.requestMatchers("/api/**").permitAll();
        authorize.anyRequest().authenticated(); // ⚠️ 已配置 anyRequest()
    });
    
    // ❌ 在 anyRequest() 之后调用 SPI 配置
    applySecurityConfigurers(http);
}

// MonitorSecurityConfigurer.java (旧版)
@Override
public void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize ->
        authorize.requestMatchers("/actuator/**").permitAll() // 💥 报错！
    );
}
```

**问题**: `anyRequest()` 必须是**最后一个**配置，之后不能再添加具体路径规则。

---

## ✅ 解决方案

### 核心思路

**将 SPI 扩展点提前到 `anyRequest()` 之前调用**，并改为传入 `AuthorizationManagerRequestMatcherRegistry` 而不是 `HttpSecurity`。

### 修改内容

#### 1. SecurityConfigurer 接口（SPI 定义）

**变更前**:
```java
public interface SecurityConfigurer {
    void configure(HttpSecurity http) throws Exception;
}
```

**变更后**:
```java
public interface SecurityConfigurer {
    void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    );
}
```

**优势**:
- ✅ 直接在同一个 `authorizeHttpRequests()` 块中配置
- ✅ 保证所有 SPI 配置在 `anyRequest()` 之前执行
- ✅ 避免 `Can't configure mvcMatchers after anyRequest` 异常

#### 2. WebSecurityConfig（核心安全配置）

**变更前**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> {
        authorize.requestMatchers("/api/**").permitAll();
        authorize.anyRequest().authenticated();
    });
    
    // ❌ anyRequest() 之后调用
    applySecurityConfigurers(http);
}

private void applySecurityConfigurers(HttpSecurity http) throws Exception {
    securityConfigurers.forEach(c -> c.configure(http));
}
```

**变更后**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> {
        authorize.requestMatchers("/api/**").permitAll();
        
        // ✅ anyRequest() 之前调用 SPI 扩展
        applySecurityConfigurers(authorize);
        
        authorize.anyRequest().authenticated();
    });
}

private void applySecurityConfigurers(
    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
) {
    securityConfigurers.forEach(c -> c.configureAuthorization(registry));
}
```

**关键改进**:
- ✅ SPI 配置在 `anyRequest()` 之前执行
- ✅ 所有配置在同一个 `authorizeHttpRequests()` 块中
- ✅ 避免多次调用 `http.authorizeHttpRequests()`

#### 3. MonitorSecurityConfigurer（Monitor 模块 SPI 实现）

**变更前**:
```java
@Override
public void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize ->
        authorize.requestMatchers("/actuator/**").permitAll()
    );
}
```

**变更后**:
```java
@Override
public void configureAuthorization(
    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
) {
    registry.requestMatchers("/actuator/health").permitAll();
    registry.requestMatchers("/actuator/info").permitAll();
    registry.requestMatchers("/actuator/prometheus").permitAll();
    registry.requestMatchers("/actuator/**").permitAll();
    
    log.info("监控模块安全配置已加载: Actuator 端点放行");
}
```

**优势**:
- ✅ 直接操作 `registry`，不需要再调用 `http.authorizeHttpRequests()`
- ✅ 配置简洁明了
- ✅ 自动在 `anyRequest()` 之前生效

---

## 📐 最终架构

### 配置顺序流程

```
1. Spring 容器启动
   ↓
2. WebSecurityConfig.securityFilterChain() 被调用
   ↓
3. http.authorizeHttpRequests(authorize -> {
       ↓
   4. 配置核心端点（auth、openapi、static）
       ↓
   5. 调用 applySecurityConfigurers(authorize) ← ✅ 关键点
       ↓
   6. 按 order 排序并调用所有 SPI 实现:
       - MonitorSecurityConfigurer.configureAuthorization(registry)
       - SystemSecurityConfigurer.configureAuthorization(registry)
       - ...
       ↓
   7. 配置 anyRequest().authenticated() ← ✅ 最后执行
   })
```

### 模块职责清晰

| 模块 | 职责 | 配置方式 |
|------|------|----------|
| **mortise-auth** | 提供安全框架和 SPI 接口 | 核心端点 + SPI 集成点 |
| **mortise-monitor** | 配置监控端点访问权限 | 实现 SecurityConfigurer SPI |
| **mortise-system** | 配置业务端点访问权限 | 实现 SecurityConfigurer SPI |
| **mortise-app** | 组装所有配置 | 依赖所有模块 |

---

## 🧪 验证方法

### 1. 编译验证

```bash
mvn clean compile -pl mortise-auth,mortise-monitor -am
```

**预期结果**: ✅ BUILD SUCCESS

### 2. 启动验证

```bash
mvn spring-boot:run -pl mortise-app -Dspring-boot.run.profiles=dev
```

**预期日志**:
```
INFO c.r.m.auth.config.WebSecurityConfig      : 发现 1 个 SecurityConfigurer 扩展
INFO c.r.m.auth.config.WebSecurityConfig      : 应用 SecurityConfigurer: MonitorSecurityConfigurer
INFO c.r.m.monitor.config.MonitorSecurityConfigurer : 监控模块安全配置已加载: Actuator 端点放行
INFO c.r.m.auth.config.WebSecurityConfig      : WebSecurityConfig 配置完成
```

### 3. 功能验证

```bash
# 验证 Actuator 端点无需认证
curl http://localhost:9999/mortise/actuator/health
# 预期: 200 OK

curl http://localhost:9999/mortise/actuator/prometheus
# 预期: 200 OK

# 验证 Swagger UI 无需认证
curl http://localhost:9999/mortise/swagger-ui.html
# 预期: 200 OK

# 验证业务端点需要认证
curl http://localhost:9999/mortise/api/v1/users
# 预期: 401 Unauthorized
```

---

## 📚 扩展示例

### 如何在其他模块中使用 SPI

```java
// mortise-system/config/SystemSecurityConfigurer.java
@Component
public class SystemSecurityConfigurer implements SecurityConfigurer {
    
    @Override
    public int getOrder() {
        return 100; // 普通优先级
    }
    
    @Override
    public void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        // 配置系统管理端点 - 需要 ADMIN 角色
        registry.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
        
        // 配置用户端点 - 需要认证
        registry.requestMatchers("/api/v1/users/**").authenticated();
        
        log.info("系统模块安全配置已加载");
    }
}
```

---

## 🎯 关键要点

### 1. SPI 设计原则

- ✅ **单一职责**: 每个模块只配置自己的端点
- ✅ **优先级控制**: 通过 `getOrder()` 控制配置顺序
- ✅ **松耦合**: 模块之间通过 SPI 接口通信
- ✅ **可扩展**: 任何模块都可以提供安全配置

### 2. 配置顺序原则

```java
// ✅ 正确顺序
registry.requestMatchers("/specific/path").permitAll();  // 具体路径
registry.requestMatchers("/api/**").hasRole("USER");     // 模式匹配
registry.anyRequest().authenticated();                    // 兜底规则（最后）

// ❌ 错误顺序
registry.anyRequest().authenticated();                    // 兜底规则
registry.requestMatchers("/specific/path").permitAll();  // 💥 报错！
```

### 3. SPI 接口选择

| 方案 | 接口参数 | 优点 | 缺点 |
|------|----------|------|------|
| ❌ 旧方案 | `HttpSecurity` | 灵活性高 | 容易违反顺序约束 |
| ✅ 新方案 | `AuthorizationManagerRequestMatcherRegistry` | 保证顺序正确 | 只能配置授权规则 |

---

## 📝 总结

### 问题根源
- Spring Security 要求 `anyRequest()` 必须最后配置
- 旧实现在 `anyRequest()` 之后调用 SPI 扩展

### 解决方案
- **SPI 接口改进**: `configure(HttpSecurity)` → `configureAuthorization(Registry)`
- **调用时机提前**: `anyRequest()` 之后 → `anyRequest()` 之前
- **配置方式优化**: 多次 `authorizeHttpRequests()` → 单次 `authorizeHttpRequests()`

### 架构优势
- ✅ 保持模块解耦（monitor 不依赖 auth）
- ✅ 保证配置顺序正确（避免运行时异常）
- ✅ 提供清晰的扩展点（SPI 模式）
- ✅ 支持优先级控制（order 机制）

---

**修复完成！现在可以正常启动应用了** 🎉
