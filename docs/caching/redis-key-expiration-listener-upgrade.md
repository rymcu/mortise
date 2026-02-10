# RedisKeyExpirationListener 统一缓存配置适配说明

## 概述

在使用 `CacheConfig` 统一管理缓存配置后，`RedisKeyExpirationListener` 已经进行了相应调整，以适应新的缓存键命名规范和管理方式。

## 主要变更

### 1. 支持多种键格式

监听器现在支持处理两种格式的用户在线状态缓存键：

- **旧格式**：`last_online:account`
- **新格式**：`mortise:cache:userOnlineStatus:account`

这样做的好处是确保了向后兼容性，在迁移过程中不会丢失任何过期事件的处理。

### 2. 增强的错误处理

```java
// 新增了异常处理机制
try {
    boolean flag = userService.updateLastOnlineTimeByAccount(account);
    log.info("更新用户 {} 最后在线时间结果：{}", account, flag ? "成功" : "失败");
} catch (Exception e) {
    log.error("更新用户 {} 最后在线时间失败", account, e);
}
```

### 3. 扩展的缓存过期处理

新增了 `handleOtherCacheExpiration` 方法，可以处理其他类型的缓存过期事件：

- JWT Token 缓存过期
- 用户会话缓存过期
- 临时数据缓存过期

## 配置要求

### 1. Redis 过期事件通知

确保 Redis 配置中启用了键过期事件通知：

```bash
# redis.conf 或通过命令行
config set notify-keyspace-events Ex
```

### 2. 缓存配置一致性

确保使用的缓存键与 `CacheConstant` 中定义的常量保持一致：

```java
// 在使用缓存时，应该使用统一的缓存名称
@Cacheable(value = CacheConstant.USER_ONLINE_STATUS_CACHE, key = "#account")
public void updateUserOnlineStatus(String account) {
    // 业务逻辑
}
```

## 使用示例

### 1. 设置用户在线状态

```java
@Service
public class UserOnlineService {
    
    @Autowired
    private CacheManager cacheManager;
    
    public void setUserOnline(String account) {
        Cache cache = cacheManager.getCache(CacheConstant.USER_ONLINE_STATUS_CACHE);
        if (cache != null) {
            cache.put(account, System.currentTimeMillis());
        }
    }
}
```

### 2. 自定义过期处理

如果需要添加其他类型的过期处理逻辑，可以在 `handleOtherCacheExpiration` 方法中扩展：

```java
private void handleOtherCacheExpiration(String expiredKey) {
    // 自定义缓存过期处理
    if (expiredKey.contains("your-custom-cache")) {
        // 处理自定义缓存过期
        log.info("自定义缓存过期：{}", expiredKey);
        // 添加自定义处理逻辑
    }
    
    // 现有的处理逻辑...
}
```

## 迁移建议

### 1. 渐进式迁移

- 保持新旧两种键格式的兼容性
- 逐步将应用中的缓存操作迁移到使用 `CacheConstant` 定义的缓存名称
- 监控日志确保过期事件正常处理

### 2. 性能优化

- 使用 `DEBUG` 级别记录详细的过期事件信息
- 只有在需要时才记录 `INFO` 级别的日志
- 异常情况记录 `ERROR` 级别日志

### 3. 监控指标

建议添加监控指标来跟踪：

- 过期事件的处理频率
- 用户在线状态更新的成功率
- 异常情况的发生频率

## 注意事项

1. **Redis 配置**：确保 Redis 实例配置了键过期事件通知
2. **性能影响**：键过期事件会增加 Redis 的负载，在高并发场景下需要注意性能影响
3. **数据一致性**：过期事件处理是异步的，可能存在轻微的延迟
4. **异常处理**：确保 `UserService.updateLastOnlineTimeByAccount` 方法具有适当的异常处理机制

## 测试验证

### 1. 功能测试

```java
@Test
public void testUserOnlineStatusExpiration() {
    // 设置用户在线状态
    setUserOnlineStatus("testUser");
    
    // 等待过期
    Thread.sleep(31000); // 等待30分钟+1秒
    
    // 验证过期处理是否执行
    // 检查数据库中的最后在线时间是否更新
}
```

### 2. 兼容性测试

验证新旧两种键格式都能正确处理过期事件。

## 相关文件

- `CacheConfig.java` - 统一缓存配置
- `CacheConstant.java` - 缓存常量定义  
- `JwtConstants.java` - JWT相关常量
- `RedisListenerConfig.java` - Redis监听器配置

## 总结

通过这次调整，`RedisKeyExpirationListener` 现在能够：

1. 兼容新旧两种缓存键格式
2. 提供更好的错误处理和日志记录
3. 支持扩展其他类型的缓存过期处理
4. 与统一的缓存配置架构保持一致

这确保了系统在迁移到统一缓存配置后的稳定性和可维护性。