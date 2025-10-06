# 微信模块配置总结

## ✅ 推荐配置方案（当前使用）

### 核心依赖
```xml
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-mp</artifactId>
</dependency>
```

### 配置类
- **WeChatMpConfiguration** - 微信公众号配置（基于数据库）
- **WeChatOpenConfiguration** - 微信开放平台配置（基于数据库）
- **WeChatMultiAccountConfigService** - 配置加载服务

### 配置文件（application.yml）
```yaml
# 简单开关即可
wechat:
  mp:
    enabled: true    # 启用微信公众号
  open:
    enabled: true    # 启用微信开放平台（扫码登录）

# Redis 配置（可选，用于存储 AccessToken）
spring:
  data:
    redis:
      host: 192.168.21.238
      port: 6379
      password: your-password
      database: 1
```

### 数据库配置
通过 REST API 或直接插入数据库：
```bash
POST /api/wechat/admin/accounts
{
  "accountType": "mp",
  "accountName": "官方公众号",
  "appId": "wx1234567890abcdef",
  "appSecret": "your-app-secret",
  "isDefault": true,
  "isEnabled": true
}
```

## ❌ 已废弃的配置方案

### 依赖（已移除）
```xml
<!-- 不再使用 -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>wx-java-mp-spring-boot-starter</artifactId>
</dependency>
```

### 配置类（已删除）
- ~~WxMpStandardConfiguration~~ - 已删除
- ~~@Component on WeChatMpProperties~~ - 已移除
- ~~@ConfigurationProperties on WeChatMpProperties~~ - 已移除

### 配置文件（不再使用）
```yaml
# ❌ 不要这样配置
wx:
  mp:
    configs:
      - app-id: xxx
        secret: xxx
```

## 架构优势

| 特性 | 旧方案 | 新方案 |
|------|--------|--------|
| 配置来源 | application.yml | 数据库 |
| 多账号 | ❌ 复杂 | ✅ 简单 |
| 动态配置 | ❌ 需重启 | ✅ 支持（需重启）|
| 敏感信息 | ❌ 明文 | ✅ 加密 |
| 配置管理 | ❌ 手动编辑文件 | ✅ API/管理界面 |
| Redis 管理 | ❌ 独立配置 | ✅ 统一管理 |
| Bean 冲突 | ❌ 可能冲突 | ✅ 无冲突 |
| 优雅降级 | ❌ 启动失败 | ✅ 自动降级 |

## 快速参考

### 启用微信功能
```yaml
wechat:
  mp:
    enabled: true
```

### 禁用微信功能
```yaml
wechat:
  mp:
    enabled: false
```

### 使用微信服务
```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final Optional<WxMpService> wxMpService;
    
    public void sendMessage() {
        if (wxMpService.isEmpty()) {
            log.warn("微信服务未初始化");
            return;
        }
        // 使用服务
        wxMpService.get().getMsgService().sendTextMessage(...);
    }
}
```

## 详细文档

- [配置简化说明](./CONFIGURATION_SIMPLIFICATION.md) - 详细的架构演变
- [优雅启动修复](./GRACEFUL_STARTUP_FIX.md) - 启动异常处理
- [Redis 统一管理](./REDIS_UNIFIED_MANAGEMENT.md) - Redis 配置方案
- [多账号使用指南](./WECHAT_MULTI_ACCOUNT_GUIDE.md) - 多账号管理

## 常见问题

### Q: 为什么移除了 @Component 注解？
A: `WeChatMpProperties` 不是 Bean，是由 `WeChatMultiAccountConfigService` 从数据库动态创建的 DTO。

### Q: 为什么删除了 WxMpStandardConfiguration？
A: 改用 `weixin-java-mp` 核心库，不需要 starter 的自动配置。使用基于数据库的 `WeChatMpConfiguration` 更灵活。

### Q: 配置能否热更新？
A: 当前需要重启应用。未来可以考虑实现配置热更新。

### Q: Redis 是否必须？
A: 开发环境可选，生产环境推荐（支持分布式）。

### Q: 如何迁移旧配置？
A: 参考 [配置简化说明](./CONFIGURATION_SIMPLIFICATION.md) 中的迁移指南。
