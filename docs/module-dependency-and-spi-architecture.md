# 模块依赖与 SPI 扩展架构说明

## 🎯 核心问题

**问题**: `mortise-monitor` 模块是否应该引入 `mortise-auth` 实现 Actuator 端点放行配置？

**答案**: ❌ **不应该直接依赖**，但可以通过 **SPI 扩展机制** 实现配置。

---

## 📐 架构原则

### 1. 模块分层原则

```
第 5 层: mortise-app (应用层)
         └── 聚合所有模块

第 4 层: mortise-system (业务层)
         └── 依赖所有基础模块

第 3 层: mortise-auth, mortise-web, mortise-monitor (应用基础层)
         └── 同级模块，相互不依赖
         └── 依赖基础设施层

第 2 层: mortise-log, mortise-cache, mortise-notification (基础设施层)
         └── 依赖核心层

第 1 层: mortise-common, mortise-core (核心层)
         └── 无依赖
```

### 2. 同层模块不互相依赖

✅ **正确**:
```
mortise-monitor → mortise-common ✓
mortise-auth → mortise-cache ✓
mortise-web → mortise-core ✓
```

❌ **错误**:
```
mortise-monitor → mortise-auth ✗  (同层依赖)
mortise-auth → mortise-web ✗     (同层依赖)
mortise-web → mortise-monitor ✗  (同层依赖)
```

---

## ✅ 解决方案: SPI 扩展机制

### 实现步骤

#### 步骤 1: mortise-auth 提供 SPI 接口

```java
// mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/SecurityConfigurer.java
public interface SecurityConfigurer {
    int getOrder();
    void configure(HttpSecurity http) throws Exception;
    boolean isEnabled();
}
```

#### 步骤 2: mortise-monitor 实现 SPI 接口

```java
// mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/MonitorSecurityConfigurer.java
@Component
public class MonitorSecurityConfigurer implements SecurityConfigurer {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize ->
            authorize.requestMatchers("/actuator/**").permitAll()
        );
    }
}
```

#### 步骤 3: mortise-auth 自动发现并应用

```java
// mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java
@Autowired
public WebSecurityConfig(Optional<List<SecurityConfigurer>> configurersOptional) {
    this.securityConfigurers = configurersOptional.orElse(null);
}

private void applySecurityConfigurers(HttpSecurity http) {
    securityConfigurers.stream()
        .filter(SecurityConfigurer::isEnabled)
        .sorted(Comparator.comparingInt(SecurityConfigurer::getOrder))
        .forEach(c -> c.configure(http));
}
```

---

## 📦 依赖配置

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

    <!-- Spring Security（optional，仅用于 HttpSecurity 接口） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 关键点说明

1. **`<optional>true</optional>`**: 
   - ✅ 编译时可用（提供 SPI 接口和类型）
   - ✅ 运行时不强制依赖（由 mortise-app 提供）
   - ✅ 避免传递依赖污染

2. **运行时依赖由 mortise-app 提供**:
   ```xml
   <!-- mortise-app/pom.xml -->
   <dependency>
       <groupId>com.rymcu</groupId>
       <artifactId>mortise-monitor</artifactId>
   </dependency>
   <dependency>
       <groupId>com.rymcu</groupId>
       <artifactId>mortise-auth</artifactId>
   </dependency>
   ```

---

## 🎯 设计优势

### 1. 解耦性 ✅
- monitor 模块不强依赖 auth 模块
- 可以独立使用 monitor 模块（如果不需要安全配置）
- SPI 接口作为契约，实现松耦合

### 2. 扩展性 ✅
```java
// 任何模块都可以提供 SecurityConfigurer 实现
@Component
public class SystemSecurityConfigurer implements SecurityConfigurer { ... }

@Component
public class WebSecurityConfigurer implements SecurityConfigurer { ... }

@Component
public class MonitorSecurityConfigurer implements SecurityConfigurer { ... }
```

### 3. 可测试性 ✅
- monitor 模块的安全配置可以独立测试
- 不需要启动整个 auth 模块

### 4. 职责清晰 ✅
- **mortise-auth**: 提供安全框架和 SPI 接口
- **mortise-monitor**: 定义自己的安全策略
- **mortise-system**: 定义业务端点的安全策略
- **mortise-app**: 组装所有配置

---

## 🔄 配置加载流程

```
1. Spring 容器启动
   ↓
2. 扫描所有 @Component
   ↓
3. 发现所有 SecurityConfigurer 实现:
   - MonitorSecurityConfigurer (order=50)
   - SystemSecurityConfigurer (order=200)
   ↓
4. WebSecurityConfig 收集所有实现
   ↓
5. 按 order 排序并应用到 HttpSecurity
   ↓
6. 生成最终的 SecurityFilterChain
```

---

## 🚫 反模式示例

### ❌ 错误做法 1: 直接依赖

```xml
<!-- mortise-monitor/pom.xml -->
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-auth</artifactId>
    <!-- 不加 optional，强制依赖 -->
</dependency>
```

**问题**:
- 破坏模块分层
- 可能导致循环依赖
- 增加耦合度

### ❌ 错误做法 2: 在 auth 模块硬编码 monitor 配置

```java
// mortise-auth/.../WebSecurityConfig.java
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.authorizeHttpRequests(authorize -> {
        // 硬编码 monitor 的配置 ❌
        authorize.requestMatchers("/actuator/**").permitAll();
    });
}
```

**问题**:
- auth 模块不应该知道 monitor 模块的存在
- 违反单一职责原则
- 配置分散，难以维护

---

## ✅ 最佳实践总结

### 1. 模块职责

| 模块 | 职责 | 依赖 |
|------|------|------|
| mortise-auth | 提供安全框架和 SPI | common, core, cache, log |
| mortise-monitor | 提供监控功能和配置 | common, (auth SPI optional) |
| mortise-app | 组装所有模块 | 所有模块 |

### 2. 配置策略

```java
// 每个模块管理自己的安全策略
@Component
public class XxxSecurityConfigurer implements SecurityConfigurer {
    @Override
    public void configure(HttpSecurity http) {
        // 配置自己模块的端点访问权限
    }
}
```

### 3. 依赖声明

```xml
<!-- 仅在需要 SPI 接口时声明 optional 依赖 -->
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-auth</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 📚 相关文档

- **SPI 接口定义**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/SecurityConfigurer.java`
- **Monitor 配置实现**: `mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/MonitorSecurityConfigurer.java`
- **Auth 核心配置**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java`
- **安全配置指南**: `docs/security-configuration-guide.md`

---

## 🎓 架构设计原则总结

1. ✅ **依赖倒置**: 依赖抽象（SPI接口），不依赖具体实现
2. ✅ **单一职责**: 每个模块只负责自己的安全配置
3. ✅ **开闭原则**: 通过 SPI 扩展，不修改 auth 核心代码
4. ✅ **接口隔离**: SecurityConfigurer 接口简洁明确
5. ✅ **最少知识**: monitor 不需要知道 auth 的内部实现

---

**结论**: 通过 SPI 扩展机制，`mortise-monitor` 可以定义自己的安全配置，而**不需要强依赖** `mortise-auth` 模块，实现了模块间的解耦和灵活扩展！🎉
