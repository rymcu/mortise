# 微信模块 Redis 统一管理方案

## 问题背景

微信模块需要使用 Redis 来存储 AccessToken 和配置信息，原先的实现直接创建 `RedisTemplateWxRedisOps` 实例，没有复用项目的 Redis 配置，存在以下问题：

1. **重复配置**：微信模块单独管理 Redis 连接，与项目主 Redis 配置不一致
2. **资源浪费**：可能创建多个 Redis 连接池
3. **配置分散**：Redis 配置分散在多个地方，难以统一管理
4. **维护困难**：Redis 升级或配置调整需要修改多处

## 解决方案

### 1. 统一使用项目的 Redis 配置

**修改前**：
```java
@Configuration
public class WxMpStandardConfiguration {
    private final WeChatMpProperties properties;
    
    private WxMpDefaultConfigImpl createConfigStorage(Config config) {
        // 直接创建新的 RedisTemplateWxRedisOps，没有使用项目的 Redis 配置
        configStorage = new WxMpRedisConfigImpl(new RedisTemplateWxRedisOps(), config.getAppId());
    }
}
```

**修改后**：
```java
@Configuration
@RequiredArgsConstructor
public class WxMpStandardConfiguration {
    private final WeChatMpProperties properties;
    private final Optional<StringRedisTemplate> stringRedisTemplate; // 注入项目的 Redis 模板
    
    private WxMpDefaultConfigImpl createConfigStorage(Config config) {
        if (properties.isUseRedis() && stringRedisTemplate.isPresent()) {
            // 使用项目统一的 Redis 管理
            configStorage = new WxMpRedisConfigImpl(
                new RedisTemplateWxRedisOps(stringRedisTemplate.get()), 
                config.getAppId()
            );
        } else {
            // 降级为内存存储
            configStorage = new WxMpDefaultConfigImpl();
        }
    }
}
```

### 2. Redis 配置层级

项目采用三层 Redis 配置架构：

```
┌─────────────────────────────────────────────┐
│  Application Level (application.yml)        │
│  统一的 Redis 连接配置                        │
│  - spring.redis.host                        │
│  - spring.redis.port                        │
│  - spring.redis.password                    │
│  - spring.redis.database                    │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│  Cache Module (mortise-cache)               │
│  提供 RedisTemplate Bean                     │
│  - RedisTemplate<String, Object>            │
│  - StringRedisTemplate                      │
│  - CacheManager                             │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│  Business Modules                           │
│  注入并使用 RedisTemplate                     │
│  - mortise-wechat                           │
│  - mortise-system                           │
│  - mortise-auth                             │
└─────────────────────────────────────────────┘
```

### 3. 配置示例

#### application.yml
```yaml
spring:
  data:
    redis:
      host: 192.168.21.238
      port: 6379
      password: ENC(your-encrypted-password)
      database: 1
      timeout: 3000
      jedis:
        pool:
          max-active: 8
          max-wait: 1
          max-idle: 500
          min-idle: 0
```

#### 微信模块配置
```yaml
# 微信公众号配置（基于 wx.mp 前缀，用于 starter 兼容）
wx:
  mp:
    use-redis: true        # 启用 Redis 存储（会自动使用项目的 Redis 配置）
    configs:
      - app-id: wx1234567890abcdef
        secret: your-app-secret
        token: your-token
        aes-key: your-aes-key
        account-name: 默认公众号
        enabled: true
```

### 4. Redis 存储说明

#### 微信 AccessToken 存储
使用 Redis 存储微信 AccessToken 的好处：

1. **分布式部署**：多实例共享 AccessToken，避免重复获取
2. **持久化**：Redis 重启后 Token 仍然有效（未过期）
3. **过期管理**：自动处理 Token 过期

存储结构：
```
# Key 格式
wx:access_token:{appId}

# Value 格式（JSON）
{
  "accessToken": "67_xxx...",
  "expiresTime": 1728234567890
}
```

#### 微信 JsApiTicket 存储
```
# Key 格式
wx:jsapi_ticket:{appId}

# Value 格式（JSON）
{
  "jsapiTicket": "kgt8ON7yVITDhtdwci0qeXxx...",
  "expiresTime": 1728234567890
}
```

### 5. 优势总结

#### 统一管理
- ✅ 所有 Redis 连接统一在 `application.yml` 配置
- ✅ 使用项目统一的 Redis 连接池
- ✅ 统一的序列化策略
- ✅ 统一的异常处理

#### 资源优化
- ✅ 共享 Redis 连接池，减少资源消耗
- ✅ 避免重复连接，提高性能
- ✅ 统一的连接池配置（最大连接数、超时时间等）

#### 可维护性
- ✅ Redis 配置集中管理，修改方便
- ✅ 支持配置加密（Jasypt）
- ✅ 支持多环境配置（dev/prod）
- ✅ 便于监控和问题排查

#### 灵活性
- ✅ 支持优雅降级：Redis 不可用时自动切换到内存存储
- ✅ 使用 `Optional` 注入，避免启动失败
- ✅ 可以通过配置开关控制是否使用 Redis

### 6. 最佳实践

#### 开发环境
```yaml
# 可以不使用 Redis，提高开发效率
wx:
  mp:
    use-redis: false  # 使用内存存储
```

#### 生产环境
```yaml
# 必须使用 Redis，支持分布式部署
wx:
  mp:
    use-redis: true   # 使用 Redis 存储
```

#### 监控建议
1. 监控 Redis 连接池使用情况
2. 监控微信 AccessToken 刷新频率
3. 设置 Redis 慢查询日志
4. 配置 Redis 告警规则

### 7. 故障处理

#### Redis 不可用
- **表现**：微信功能自动降级为内存存储
- **影响**：单实例模式下功能正常，集群模式下可能重复获取 Token
- **恢复**：Redis 恢复后自动切换回 Redis 存储

#### AccessToken 丢失
- **原因**：Redis 数据被清空或过期
- **处理**：自动重新获取 AccessToken
- **预防**：设置合理的 Redis 持久化策略

### 8. 相关文件

#### 核心配置
- `mortise-cache/src/main/java/com/rymcu/mortise/cache/config/BaseCacheConfig.java`
  - 提供统一的 `RedisTemplate` 和 `StringRedisTemplate` Bean
  
- `mortise-wechat/src/main/java/com/rymcu/mortise/wechat/config/WxMpStandardConfiguration.java`
  - 注入 `StringRedisTemplate`，创建微信 Redis 配置存储

#### 配置文件
- `application.yml` - 主配置文件
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置

### 9. 注意事项

1. **Redis 版本**：确保 Redis 版本 >= 3.2
2. **序列化**：微信 SDK 使用 String 序列化，与项目默认的 Jackson 序列化不同
3. **数据库隔离**：建议微信数据使用独立的 Redis database
4. **Key 命名**：微信 SDK 自动添加前缀，避免 Key 冲突

### 10. 迁移指南

#### 从独立 Redis 配置迁移

**步骤 1**：移除旧的 Redis 配置
```yaml
# 删除或注释掉
wx:
  mp:
    redis-config:
      host: xxx
      port: xxx
```

**步骤 2**：确保项目主 Redis 配置正确
```yaml
spring:
  data:
    redis:
      host: your-redis-host
      port: 6379
```

**步骤 3**：启用微信模块的 Redis 存储
```yaml
wx:
  mp:
    use-redis: true
```

**步骤 4**：重启应用，检查日志
```
✓ 使用 Redis 存储微信公众号配置，AppID: wx12***cdef
✓ 公众号配置初始化完成，AppID: wx12***cdef, AccountName: 默认公众号
```

## 总结

通过统一管理 Redis，微信模块的配置更加简洁，资源利用更加高效，维护也更加方便。这是一个典型的**依赖注入**和**配置统一化**的最佳实践。
