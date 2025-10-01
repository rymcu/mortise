# 缓存失效监听 SPI 机制使用指南

## 概述

基于 SPI（Service Provider Interface）机制实现缓存失效时间的监听和处理，提供了一个可扩展的架构，允许各业务模块独立处理自己的缓存失效事件，而无需修改核心缓存模块代码。

## 架构设计

### 核心组件

1. **CacheExpirationHandler（SPI 接口）**：定义缓存失效处理的标准接口
2. **RedisKeyExpirationListener（监听器）**：统一的 Redis 键过期事件监听器
3. **RedisListenerConfig（配置类）**：Redis 监听器配置

### 模块分布

- **mortise-cache**：提供 SPI 接口和基础监听器
- **业务模块**：实现具体的缓存失效处理逻辑

## 使用方法

### 步骤 1：实现 CacheExpirationHandler 接口

在业务模块中创建处理器类：

```java
@Component
public class UserCacheExpirationHandler implements CacheExpirationHandler {

    @Autowired
    private UserService userService;

    @Override
    public int getOrder() {
        return 10; // 设置优先级，数字越小优先级越高
    }

    @Override
    public boolean supports(String expiredKey) {
        // 判断是否支持处理该失效键
        return expiredKey.contains("userOnlineStatus:");
    }

    @Override
    public void handle(String expiredKey) {
        // 处理缓存失效事件
        String account = extractAccount(expiredKey);
        userService.updateLastOnlineTime(account);
    }

    @Override
    public String getName() {
        return "用户缓存失效处理器";
    }

    private String extractAccount(String key) {
        // 提取用户账号的逻辑
        return key.substring(key.lastIndexOf(":") + 1);
    }
}
```

### 步骤 2：配置 Redis 键过期通知

确保 Redis 配置启用键过期事件通知：

```bash
# redis.conf 配置
notify-keyspace-events Ex

# 或者通过命令行设置
redis-cli config set notify-keyspace-events Ex
```

### 步骤 3：注册为 Spring Bean

确保处理器类被 Spring 扫描到（使用 `@Component` 或其他注解）。

## 配置示例

### application.yml 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    password: ${REDIS_PASSWORD:}
    connection-timeout: 2000
    so-timeout: 2000

# 线程池配置（可选）
executor:
  thread:
    async:
      corePoolSize: 10
      maxPoolSize: 20
      queueCapacity: 200
      keepAliveSeconds: 60
      threadNamePrefix: "mortise-async-"
```

## 业务模块实现示例

### 用户模块示例

```java
// mortise-system/src/main/java/com/rymcu/mortise/system/handler/UserOnlineStatusExpirationHandler.java
@Component
@Slf4j
public class UserOnlineStatusExpirationHandler implements CacheExpirationHandler {

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(String expiredKey) {
        return expiredKey.contains(CacheConstant.CACHE_NAME_PREFIX + SystemCacheConstant.USER_ONLINE_STATUS_CACHE + ":");
    }

    @Override
    public void handle(String expiredKey) {
        String account = extractAccountFromKey(expiredKey);
        if (account != null) {
            try {
                boolean success = userService.updateLastOnlineTimeByAccount(account);
                log.info("更新用户 {} 最后在线时间: {}", account, success ? "成功" : "失败");
            } catch (Exception e) {
                log.error("更新用户 {} 最后在线时间失败", account, e);
            }
        }
    }

    private String extractAccountFromKey(String key) {
        String prefix = CacheConstant.CACHE_NAME_PREFIX + SystemCacheConstant.USER_ONLINE_STATUS_CACHE + ":";
        return key.startsWith(prefix) ? key.substring(prefix.length()) : null;
    }
}
```

### 认证模块示例

```java
// mortise-auth/src/main/java/com/rymcu/mortise/auth/handler/TokenExpirationHandler.java
@Component
@Slf4j
public class TokenExpirationHandler implements CacheExpirationHandler {

    @Override
    public int getOrder() {
        return 20; // 较低优先级
    }

    @Override
    public boolean supports(String expiredKey) {
        return expiredKey.contains(AuthCacheConstant.JWT_TOKEN_CACHE);
    }

    @Override
    public void handle(String expiredKey) {
        log.debug("JWT Token 缓存过期：{}", expiredKey);
        // 可以在这里添加 Token 过期的额外处理逻辑
        // 比如记录审计日志、通知用户等
    }
}
```

## 优势特点

### 1. 模块解耦
- 各业务模块独立处理自己的缓存失效事件
- 无需修改核心缓存模块代码
- 符合开闭原则

### 2. 可扩展性
- 支持多个处理器同时处理同一个失效事件
- 通过优先级控制处理顺序
- 支持动态启用/禁用处理器

### 3. 容错性
- 单个处理器失败不影响其他处理器
- 完整的异常处理和日志记录
- 保证框架默认行为正常执行

### 4. 配置灵活
- 支持通过配置文件自定义参数
- 处理器可以根据配置动态启用/禁用
- 支持自定义优先级和名称

## 注意事项

1. **Redis 配置**：必须启用键过期事件通知
2. **性能考虑**：避免在处理器中执行耗时操作
3. **异常处理**：处理器中应该有完善的异常处理机制
4. **日志记录**：建议记录详细的处理日志便于调试

## 迁移建议

### 从旧的监听器迁移

1. 将现有的 `RedisKeyExpirationListener` 中的业务逻辑提取出来
2. 按业务领域创建对应的 `CacheExpirationHandler` 实现
3. 逐步迁移和测试各个处理器
4. 删除旧的监听器代码

### 兼容性处理

在迁移过程中，可以保持对旧键格式的兼容：

```java
@Override
public boolean supports(String expiredKey) {
    // 支持新格式
    return expiredKey.contains(CacheConstant.CACHE_NAME_PREFIX + "userOnlineStatus:") ||
           // 兼容旧格式
           expiredKey.startsWith("last_online:");
}
```

## 相关文件

- **SPI 接口定义**：`mortise-cache/src/main/java/com/rymcu/mortise/cache/spi/CacheExpirationHandler.java`
- **监听器实现**：`mortise-cache/src/main/java/com/rymcu/mortise/cache/listener/RedisKeyExpirationListener.java`
- **配置类**：`mortise-cache/src/main/java/com/rymcu/mortise/cache/config/CacheAutoConfiguration.java`
- **业务处理器示例**：`mortise-system/src/main/java/com/rymcu/mortise/system/handler/UserOnlineStatusExpirationHandler.java`