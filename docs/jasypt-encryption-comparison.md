# Jasypt 加密方案对比与建议

## 问题分析

用户询问是否可以用 `JasyptUtils.encryptPassword()` 和 `decryptPassword()` 静态方法来替代 `Optional<StringEncryptor>` 依赖注入模式。

## 方案对比

### 方案1：`Optional<StringEncryptor>` 依赖注入 (当前推荐)

#### 优点 ✅
```java
@Service
public class WeChatService {
    private final Optional<StringEncryptor> stringEncryptor;
    
    private String encryptValue(String value) {
        if (stringEncryptor.isPresent()) {
            return stringEncryptor.get().encrypt(value);
        }
        return value; // 优雅降级
    }
}
```

1. **Spring 生命周期管理** - 加密器由 Spring 容器管理，单例复用
2. **性能优化** - 避免重复创建加密器对象
3. **优雅降级** - `Optional` 提供安全的空值处理
4. **可测试性** - 容易进行 Mock 和单元测试
5. **配置集中化** - 加密配置统一在 `JasyptEncryptionConfig` 中
6. **环境隔离** - 可以根据环境注入不同的配置

#### 当前实现
```java
// mortise-core/JasyptEncryptionConfig.java
@Bean(name = "jasyptStringEncryptor")
public StringEncryptor passwordEncryptor() {
    return JasyptUtils.initPasswordEncryptor(System.getenv(ProjectConstant.ENCRYPTION_KEY));
}

// 服务类中使用
private final Optional<StringEncryptor> stringEncryptor;
```

### 方案2：`JasyptUtils` 静态方法 (不推荐)

#### 优点 ✅
```java
public class SomeService {
    public void encrypt() {
        String encrypted = JasyptUtils.encryptPassword("plaintext");
        String decrypted = JasyptUtils.decryptPassword(encrypted);
    }
}
```

1. **简单直接** - 无需依赖注入，直接调用
2. **无框架依赖** - 可以在非 Spring 环境使用

#### 缺点 ❌
1. **性能问题** - 每次调用都创建新的 `PooledPBEStringEncryptor` 对象
2. **环境变量硬编码** - 直接依赖 `ENCRYPTION_KEY` 环境变量
3. **测试困难** - 静态方法难以进行 Mock 测试
4. **无法优雅降级** - 环境变量不存在时会抛出异常
5. **配置不灵活** - 无法根据不同环境使用不同配置

## 性能对比

### 依赖注入方式
```java
// 应用启动时创建一次
StringEncryptor encryptor = new PooledPBEStringEncryptor(); // 只执行一次
// 运行时复用
encryptor.encrypt(value); // 快速执行
```

### 静态方法方式
```java
// 每次调用都要执行
public static String encryptPassword(String plainPassword) {
    return encryptJasyptPassword(plainPassword, System.getenv(ENCRYPTION_KEY));
}

private static String encryptJasyptPassword(String password, String key) {
    StringEncryptor encryptor = initPasswordEncryptor(key); // 每次都创建新对象！
    return encryptor.encrypt(password);
}
```

## 最终建议

### 保持当前架构 ✅

**继续使用 `Optional<StringEncryptor>` 依赖注入模式**，原因：

1. **架构一致性** - 符合 Spring Boot 最佳实践
2. **性能优越** - 单例模式，避免重复创建对象
3. **可维护性** - 便于测试和调试
4. **扩展性** - 未来可以轻松切换加密实现

### 改进措施 ✅

我已经对现有代码进行了以下改进：

#### 1. 修复了 JasyptUtils 中的方法名错误
```java
// 修复前：方法名混乱
public static String decryptJasyptPassword(String encryptedPassword) {
    return decryptJasyptPassword(encryptedPassword, System.getenv(ProjectConstant.ENCRYPTION_KEY));
}

// 修复后：方法名清晰
public static String decryptPassword(String encryptedPassword) {
    return decryptJasyptPassword(encryptedPassword, System.getenv(ProjectConstant.ENCRYPTION_KEY));
}
```

#### 2. 增强了服务类中的错误处理
```java
private String encryptValue(String value) {
    if (value == null) {
        return null;
    }

    if (stringEncryptor.isPresent()) {
        try {
            return stringEncryptor.get().encrypt(value);
        } catch (Exception e) {
            log.error("加密失败，将使用原值: {}", e.getMessage());
            return value; // 降级处理
        }
    } else {
        log.warn("加密器未配置，无法加密值");
        return value;
    }
}
```

#### 3. 统一了加密/解密的处理逻辑
- `WeChatMultiAccountConfigService` - 配置加载时的解密
- `WeChatAccountManagementService` - 账号管理时的加密

## 使用指南

### 推荐用法
```java
@Service
public class YourService {
    private final Optional<StringEncryptor> stringEncryptor;
    
    public void saveConfig(String sensitiveValue) {
        String encryptedValue = encryptValue(sensitiveValue);
        // 保存到数据库
    }
    
    private String encryptValue(String value) {
        return stringEncryptor
            .map(encryptor -> {
                try {
                    return encryptor.encrypt(value);
                } catch (Exception e) {
                    log.error("加密失败: {}", e.getMessage());
                    return value;
                }
            })
            .orElse(value);
    }
}
```

### 仅在特殊场景使用静态方法
```java
// 仅在无法使用依赖注入的场景使用，如：
// - 静态工具类
// - 测试数据生成
// - 独立的命令行工具
public class DataMigrationTool {
    public static void main(String[] args) {
        String encrypted = JasyptUtils.encryptPassword("sensitive-data");
        System.out.println("ENC(" + encrypted + ")");
    }
}
```

## 总结

**建议保持当前的 `Optional<StringEncryptor>` 架构**，它提供了更好的：
- 🚀 **性能** (单例复用)
- 🛡️ **安全性** (优雅降级) 
- 🧪 **可测试性** (易于 Mock)
- 🏗️ **可维护性** (Spring 管理)

`JasyptUtils` 静态方法作为补充工具存在，仅在无法使用依赖注入的特殊场景下使用。