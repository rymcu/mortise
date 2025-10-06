# Bean 冲突解决方案

## 问题描述

在启动应用时遇到以下错误：

```
Parameter 2 of constructor in WeChatMultiAccountConfigService required a single bean, but 2 were found:
- jasyptStringEncryptor: defined by method 'passwordEncryptor' in JasyptEncryptionConfig
- lazyJasyptStringEncryptor: defined by method 'stringEncryptor' in EncryptablePropertyResolverConfiguration
```

## 原因分析

项目中存在两个 `StringEncryptor` Bean：

### 1. 项目自定义的 Bean
```java
@Configuration
public class JasyptEncryptionConfig {
    @Bean("jasyptStringEncryptor")
    public StringEncryptor passwordEncryptor() {
        // 自定义配置
    }
}
```

### 2. Jasypt 自动配置的 Bean
```java
// 来自 jasypt-spring-boot-starter
@Bean("lazyJasyptStringEncryptor")
public StringEncryptor stringEncryptor() {
    // 自动配置
}
```

### 冲突场景

当使用 `@RequiredArgsConstructor` 时，Lombok 会自动生成构造函数：

```java
@RequiredArgsConstructor  // ❌ 问题代码
public class WeChatMultiAccountConfigService {
    private final Optional<StringEncryptor> stringEncryptor;
    
    // Lombok 生成的构造函数无法指定使用哪个 Bean
}
```

Spring 不知道应该注入哪个 `StringEncryptor`，导致启动失败。

## 解决方案

### 方案 1：使用 @Qualifier（推荐，已采用）

手动编写构造函数，使用 `@Qualifier` 指定 Bean：

```java
@Service
public class WeChatMultiAccountConfigService {
    
    private final WeChatAccountMapper weChatAccountMapper;
    private final WeChatConfigMapper weChatConfigMapper;
    private final Optional<StringEncryptor> stringEncryptor;

    /**
     * 使用 @Qualifier 指定使用项目自定义的 jasyptStringEncryptor
     */
    public WeChatMultiAccountConfigService(
            WeChatAccountMapper weChatAccountMapper,
            WeChatConfigMapper weChatConfigMapper,
            @Qualifier("jasyptStringEncryptor") Optional<StringEncryptor> stringEncryptor) {
        this.weChatAccountMapper = weChatAccountMapper;
        this.weChatConfigMapper = weChatConfigMapper;
        this.stringEncryptor = stringEncryptor;
    }
}
```

**优点**：
- ✅ 明确指定使用哪个 Bean
- ✅ 不影响其他代码
- ✅ 清晰的依赖关系

**缺点**：
- ❌ 不能使用 `@RequiredArgsConstructor`
- ❌ 需要手动编写构造函数

### 方案 2：标记 @Primary（不推荐）

在某个 Bean 上添加 `@Primary` 注解：

```java
@Configuration
public class JasyptEncryptionConfig {
    @Bean("jasyptStringEncryptor")
    @Primary  // 设置为主 Bean
    public StringEncryptor passwordEncryptor() {
        // ...
    }
}
```

**优点**：
- ✅ 可以继续使用 `@RequiredArgsConstructor`
- ✅ 影响全局，所有地方都使用这个 Bean

**缺点**：
- ❌ 影响全局，可能影响其他模块
- ❌ Jasypt 自动配置可能有其他用途

### 方案 3：排除自动配置（不推荐）

排除 Jasypt 的自动配置：

```java
@SpringBootApplication(exclude = {
    EncryptablePropertyResolverConfiguration.class
})
public class MortiseApplication {
    // ...
}
```

**缺点**：
- ❌ 可能影响配置文件的自动解密功能
- ❌ 不推荐

## 最佳实践

### 1. 何时使用 @Qualifier

当存在以下情况时，使用 `@Qualifier`：

- ✅ 同类型多个 Bean
- ✅ 需要明确指定依赖
- ✅ 局部影响，不影响其他模块

### 2. 何时使用 @Primary

当存在以下情况时，使用 `@Primary`：

- ✅ 有一个明显的"默认"或"主要" Bean
- ✅ 希望全局默认使用某个 Bean
- ✅ 其他 Bean 仅用于特殊场景

### 3. Bean 命名规范

统一的 Bean 命名有助于避免冲突：

```java
@Bean("jasyptStringEncryptor")        // 明确的名称
@Bean("weChatStringEncryptor")        // 按用途命名
@Bean("customStringEncryptor")        // 区分来源
```

## 类似问题排查

### 问题特征

```
Parameter X of constructor in YourClass required a single bean, but N were found:
- beanName1: defined by ...
- beanName2: defined by ...
```

### 排查步骤

1. **确认冲突的 Bean**
   - 查看错误信息列出的 Bean 名称
   - 找到这些 Bean 的定义位置

2. **选择解决方案**
   - 局部冲突 → 使用 `@Qualifier`
   - 全局默认 → 使用 `@Primary`
   - 不需要某个 Bean → 排除自动配置

3. **验证修复**
   - 重新启动应用
   - 检查日志确认使用了正确的 Bean

### 调试技巧

启用 Spring 的 Bean 调试日志：

```yaml
logging:
  level:
    org.springframework.beans: DEBUG
```

查看 Bean 的创建和注入过程。

## 相关配置

### Jasypt 配置

```yaml
jasypt:
  encryptor:
    password: ${JASYPT_PASSWORD:your-secret-key}
    algorithm: PBEWithMD5AndDES
    iv-generator-classname: org.jasypt.iv.NoIvGenerator
```

### 自定义 StringEncryptor

```java
@Configuration
public class JasyptEncryptionConfig {
    
    @Bean("jasyptStringEncryptor")
    public StringEncryptor passwordEncryptor(
            @Value("${jasypt.encryptor.password}") String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}
```

## 总结

在 `WeChatMultiAccountConfigService` 中：

- ✅ 使用 `@Qualifier("jasyptStringEncryptor")` 明确指定 Bean
- ✅ 移除 `@RequiredArgsConstructor`，手动编写构造函数
- ✅ 保持代码清晰，依赖关系明确

这种方式既解决了冲突问题，又保持了代码的可维护性。

## 相关文档

- [Spring Framework - Qualifier](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-autowired-annotation-qualifiers)
- [Jasypt Spring Boot](https://github.com/ulisesbocchio/jasypt-spring-boot)
- [Bean 冲突常见问题](../COMMON_ISSUES.md)
