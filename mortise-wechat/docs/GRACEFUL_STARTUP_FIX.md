# 微信模块优雅启动修复

## 问题描述

原先的微信模块在数据库没有配置或表不存在时，会导致应用启动失败，错误信息为：
```
appid不能设置为null
```

这是因为 `wx-java-mp-spring-boot-starter` 的自动配置在没有配置的情况下仍然尝试创建 `WxMpService` bean，导致启动失败。

## 解决方案

### 1. 排除 wx-java 的自动配置

在 `MortiseApplication.java` 中排除了 `WxMpServiceAutoConfiguration` 的自动配置：

```java
@SpringBootApplication(exclude = {WxMpServiceAutoConfiguration.class})
public class MortiseApplication {
    // ...
}
```

**原因**：使用自定义的数据库配置方式，不依赖 starter 的自动配置。

### 2. 增强配置类的异常处理

#### WeChatMpConfiguration
- 在 `wxMpService()` 方法中添加 try-catch 块
- 捕获所有可能的异常（数据库表不存在、查询失败等）
- 异常时返回 `Optional.empty()`，不影响应用启动
- 日志级别从 WARN 改为 INFO，更友好的提示信息

```java
@Bean(name = "wxMpService")
@Primary
@ConditionalOnProperty(prefix = "wechat.mp", name = "enabled", havingValue = "true", matchIfMissing = false)
public Optional<WxMpService> wxMpService() {
    try {
        // ... 初始化逻辑
    } catch (Exception e) {
        log.warn("微信公众号服务初始化失败（可能是数据库表不存在），应用将正常启动但微信功能不可用: {}", e.getMessage());
        return Optional.empty();
    }
}
```

#### WeChatOpenConfiguration
- 同样的异常处理机制
- 确保开放平台服务初始化失败时不影响应用启动

### 3. 优化日志输出

- 成功初始化时使用 `✓` 符号标记
- 更清晰的层级结构
- INFO 级别的友好提示信息而非 WARN

示例日志：
```
✓ 微信公众号默认服务初始化成功（数据库配置），AppID: wx1***def
  ✓ 公众号服务初始化完成，accountId: 1, accountName: 官方公众号, appId: wx1***def
✓ 微信公众号多账号服务初始化完成，共 1 个账号
```

## 启动行为

### 场景 1：数据库表不存在
- **行为**：应用正常启动
- **日志**：`微信公众号服务初始化失败（可能是数据库表不存在），应用将正常启动但微信功能不可用`
- **影响**：微信相关功能不可用，但不影响其他业务模块

### 场景 2：数据库表存在但没有配置
- **行为**：应用正常启动
- **日志**：`微信公众号默认账号未配置或未启用，跳过初始化`
- **影响**：微信相关功能不可用，但不影响其他业务模块

### 场景 3：配置正常
- **行为**：应用正常启动，微信功能可用
- **日志**：显示初始化成功的账号信息

### 场景 4：配置被禁用 (wechat.mp.enabled=false)
- **行为**：应用正常启动
- **日志**：不会尝试初始化微信服务
- **影响**：微信功能完全关闭

## 配置说明

### application.yml 配置

```yaml
wechat:
  mp:
    enabled: true    # 启用公众号功能
  open:
    enabled: true    # 启用开放平台功能（扫码登录）
```

### 功能特性

1. **优雅降级**：数据库表不存在时，应用仍可正常启动
2. **动态配置**：从数据库加载配置，支持多账号
3. **条件启用**：通过配置文件控制功能开关
4. **异常隔离**：微信模块异常不影响其他模块
5. **友好日志**：清晰的启动日志，便于排查问题

## 依赖注入说明

所有微信服务都使用 `Optional` 包装：

```java
@Service
public class WeChatLoginService {
    private final Optional<WxMpService> wxMpService;
    private final Optional<WxOpenService> wxOpenService;
    private final Optional<Map<Long, WxMpService>> wxMpServiceMap;
    private final Optional<Map<Long, WxOpenService>> wxOpenServiceMap;
    
    // 使用时检查
    public String buildAuthorizationUrl(String redirectUri, String state) {
        if (wxMpService.isEmpty()) {
            throw new RuntimeException("微信公众号服务未初始化，请先配置微信账号");
        }
        // ... 使用服务
    }
}
```

## 最佳实践

1. **初次部署**：
   - 先启动应用（微信功能不可用）
   - 等待 Flyway 创建表结构
   - 通过 API 或 SQL 配置微信账号
   - 重启应用使配置生效

2. **生产环境**：
   - 确保数据库已有正确的微信配置
   - 设置 `wechat.mp.enabled=true` 启用功能
   - 监控启动日志确认初始化成功

3. **开发环境**：
   - 可以设置 `wechat.mp.enabled=false` 跳过微信功能
   - 或者直接不配置，应用会自动降级

## 注意事项

1. Bean 名称已更改为 `wxMpService`（原为 `wechatMpService`），与 wx-java 标准命名一致
2. 排除了 `WxMpServiceAutoConfiguration` 自动配置，避免冲突
3. 所有微信服务 Bean 都使用 `Optional` 包装，调用前需要检查
4. 异常信息会记录到日志，便于问题排查

## 相关文件

- `MortiseApplication.java` - 排除自动配置
- `WeChatMpConfiguration.java` - 公众号配置（已增强异常处理）
- `WeChatOpenConfiguration.java` - 开放平台配置（已增强异常处理）
- `application-dev.yml` - 配置示例
