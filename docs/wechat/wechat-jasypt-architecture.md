# 微信模块 Jasypt 加密配置架构说明

## 配置架构

### 当前实现
微信模块使用 **`mortise-core`** 模块中已有的 Jasypt 配置，遵循项目的统一加密策略。

### 配置文件位置
```
mortise-core/
└── src/main/java/com/rymcu/mortise/core/config/
    └── JasyptEncryptionConfig.java  ✅ 已存在
```

### 配置详情
```java
@Configuration
public class JasyptEncryptionConfig {
    
    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor passwordEncryptor() {
        return JasyptUtils.initPasswordEncryptor(System.getenv(ProjectConstant.ENCRYPTION_KEY));
    }
}
```

### 环境变量
```bash
export ENCRYPTION_KEY=your-secret-key
```

## 模块依赖关系

### 依赖链
```
mortise-wechat
    └── mortise-core (通过 pom.xml 依赖)
        └── JasyptEncryptionConfig (提供 jasyptStringEncryptor Bean)
```

### WeChat 模块使用方式
```java
@Service
public class WeChatMultiAccountConfigService {
    
    private final Optional<StringEncryptor> stringEncryptor;
    
    // Spring 自动注入 jasyptStringEncryptor Bean
    public WeChatMultiAccountConfigService(Optional<StringEncryptor> stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }
}
```

## 配置验证

### ✅ 正确配置
- `mortise-core` 提供统一的 Jasypt 配置
- `mortise-wechat` 通过依赖注入使用
- 使用 `ENCRYPTION_KEY` 环境变量
- 无重复配置

### ❌ 错误配置 (已修复)
- ~~在 `application-dev.yml` 中重复配置 jasypt~~
- ~~使用不同的环境变量名~~

## 加密使用示例

### 1. 加密敏感配置
```java
// 在服务类中
if (stringEncryptor.isPresent()) {
    String encrypted = stringEncryptor.get().encrypt("sensitive_value");
    // 存储到数据库: ENC(encrypted_value)
}
```

### 2. 解密配置
```java
// 读取配置时
if (isEncrypted && stringEncryptor.isPresent()) {
    String decrypted = stringEncryptor.get().decrypt(encryptedValue);
    return decrypted;
}
```

### 3. 数据库存储格式
```sql
-- mortise_wechat_config 表示例
INSERT INTO mortise_wechat_config (config_key, config_value, is_encrypted) 
VALUES ('appSecret', 'ENC(encrypted_app_secret)', 1);
```

## 优势

### 1. 统一管理
- 所有模块使用相同的加密配置
- 避免配置重复和不一致

### 2. 环境隔离
- 生产环境和开发环境使用不同的 `ENCRYPTION_KEY`
- 支持容器化部署的环境变量注入

### 3. 安全性
- 敏感信息加密存储
- 运行时动态解密
- 支持配置更新无需重启

### 4. 可选依赖
- 使用 `Optional<StringEncryptor>` 模式
- 开发环境可以不配置加密器（明文存储）
- 生产环境强制使用加密

## 故障排除

### 如果加密器不可用
```java
// 优雅降级
if (stringEncryptor.isEmpty()) {
    log.warn("加密器未配置，使用明文存储（仅开发环境）");
    return plainValue;
}
```

### 如果环境变量未设置
```bash
# 检查环境变量
echo $ENCRYPTION_KEY

# 设置环境变量 (Linux/Mac)
export ENCRYPTION_KEY=your-secret-key

# 设置环境变量 (Windows)
set ENCRYPTION_KEY=your-secret-key
```

### 如果解密失败
- 检查 `ENCRYPTION_KEY` 是否正确
- 确认加密时使用的是相同的密钥
- 验证存储的密文格式是否正确

---

**总结**: 微信模块正确复用了 `mortise-core` 的 Jasypt 配置，实现了统一的加密管理策略。无需额外配置，开箱即用。