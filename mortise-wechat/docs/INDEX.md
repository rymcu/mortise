# 微信模块文档索引

## 📖 文档导航

### 🎯 重构文档（NEW）⭐
- **[重构完成通知](../REFACTORING_README.md)** - 重构概览和成果展示
- **[重构总结](./REFACTORING_SUMMARY.md)** - 详细的重构说明
- **[重构方案](./REFACTORING_PLAN.md)** - 完整的重构实施方案
- **[架构演进图](./ARCHITECTURE_DIAGRAM.md)** - 重构前后架构对比
- **[快速上手指南](./QUICK_START.md)** - 5分钟快速开始

### 快速开始
- [README.md](../README.md) - 模块概述和快速开始
- [CONFIGURATION_SUMMARY.md](./CONFIGURATION_SUMMARY.md) - 配置快速参考 ⭐

### 架构和设计
- [CONFIGURATION_SIMPLIFICATION.md](./CONFIGURATION_SIMPLIFICATION.md) - 配置简化说明
- [REDIS_UNIFIED_MANAGEMENT.md](./REDIS_UNIFIED_MANAGEMENT.md) - Redis 统一管理方案
- [WECHAT_MULTI_ACCOUNT_GUIDE.md](./WECHAT_MULTI_ACCOUNT_GUIDE.md) - 多账号使用指南

### 问题修复
- [FIX_SUMMARY.md](./FIX_SUMMARY.md) - 修复总结 ⭐
- [GRACEFUL_STARTUP_FIX.md](./GRACEFUL_STARTUP_FIX.md) - 优雅启动修复
- [BEAN_CONFLICT_RESOLUTION.md](./BEAN_CONFLICT_RESOLUTION.md) - Bean 冲突解决方案
- [REDIS_CONFIG_GUIDE.md](./REDIS_CONFIG_GUIDE.md) - Redis 配置指南

### 重构记录
- [WECHAT_REFACTOR_SUMMARY.md](./WECHAT_REFACTOR_SUMMARY.md) - 重构总结

## 📚 推荐阅读顺序

### 新手入门
1. [重构完成通知](../REFACTORING_README.md) - 了解重构概况 ⭐
2. [快速上手指南](./QUICK_START.md) - 5分钟快速开始 ⭐
3. [架构演进图](./ARCHITECTURE_DIAGRAM.md) - 理解架构变化

### 深入学习
1. [重构总结](./REFACTORING_SUMMARY.md) - 详细了解重构内容
2. [重构方案](./REFACTORING_PLAN.md) - 完整实施方案
3. [微信多账号指南](WECHAT_MULTI_ACCOUNT_GUIDE.md) - 多账号使用

## 🎯 常见问题

### 启动相关

#### Q1: 应用启动失败，提示 "appid不能设置为null"
**解决方案**：
1. 检查是否已排除 `WxMpServiceAutoConfiguration`
2. 查看 [GRACEFUL_STARTUP_FIX.md](./GRACEFUL_STARTUP_FIX.md)

#### Q2: Bean 冲突：There is more than one bean
**解决方案**：
1. 检查是否移除了 `@Component` 和 `@ConfigurationProperties`
2. 查看 [BEAN_CONFLICT_RESOLUTION.md](./BEAN_CONFLICT_RESOLUTION.md)

#### Q3: StringEncryptor Bean 冲突
**解决方案**：
1. 使用 `@Qualifier("jasyptStringEncryptor")` 指定 Bean
2. 查看 [BEAN_CONFLICT_RESOLUTION.md](./BEAN_CONFLICT_RESOLUTION.md)

### 配置相关

#### Q1: 如何配置微信功能？
**答案**：
```yaml
wechat:
  mp:
    enabled: true
```
详见 [CONFIGURATION_SUMMARY.md](./CONFIGURATION_SUMMARY.md)

#### Q2: Redis 如何配置？
**答案**：
只需配置 Spring 的 Redis，微信模块会自动使用。
详见 [REDIS_UNIFIED_MANAGEMENT.md](./REDIS_UNIFIED_MANAGEMENT.md)

#### Q3: 如何添加微信账号？
**答案**：
通过 REST API 或直接插入数据库。
详见 [WECHAT_MULTI_ACCOUNT_GUIDE.md](./WECHAT_MULTI_ACCOUNT_GUIDE.md)

### 功能相关

#### Q1: 支持多账号吗？
**答案**：
✅ 支持。可以配置多个公众号和开放平台账号。
详见 [WECHAT_MULTI_ACCOUNT_GUIDE.md](./WECHAT_MULTI_ACCOUNT_GUIDE.md)

#### Q2: 配置能热更新吗？
**答案**：
❌ 当前需要重启应用。未来可考虑支持。

#### Q3: 开发环境必须使用 Redis 吗？
**答案**：
❌ 不必须。可以使用内存存储。生产环境建议使用 Redis。

## 🔧 核心概念

### 配置类型

| 类型 | 用途 | 是否 Bean |
|------|------|----------|
| `WeChatMpConfiguration` | 公众号配置 | ✅ Configuration |
| `WeChatOpenConfiguration` | 开放平台配置 | ✅ Configuration |
| `WeChatMpProperties` | 属性类（DTO） | ❌ 非 Bean |
| `WeChatOpenProperties` | 属性类（DTO） | ❌ 非 Bean |

### 服务类型

| 服务 | 用途 | Bean 类型 |
|------|------|----------|
| `WxMpService` | 公众号服务 | `Optional<WxMpService>` |
| `WxOpenService` | 开放平台服务 | `Optional<WxOpenService>` |
| `WeChatMultiAccountConfigService` | 配置加载 | `@Service` |

### 配置来源

```
数据库表
├── mortise_wechat_account     (账号基本信息)
└── mortise_wechat_config      (账号扩展配置)
     ↓
WeChatMultiAccountConfigService (加载配置)
     ↓
创建 WeChatMpProperties (DTO)
     ↓
WeChatMpConfiguration (创建 Bean)
     ↓
Optional<WxMpService> (可注入使用)
```

## 🚀 最佳实践

### 开发环境
```yaml
# 简化配置，不使用 Redis
wechat:
  mp:
    enabled: true  # 或 false 完全禁用
```

### 生产环境
```yaml
# 完整配置，使用 Redis
wechat:
  mp:
    enabled: true

spring:
  data:
    redis:
      host: your-redis-host
      port: 6379
      password: your-password
```

### 使用服务
```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final Optional<WxMpService> wxMpService;
    
    public void doSomething() {
        // 检查服务是否可用
        if (wxMpService.isEmpty()) {
            log.warn("微信服务未初始化");
            return;
        }
        
        // 使用服务
        wxMpService.get().getMsgService().sendTextMessage(...);
    }
}
```

## 📊 版本历史

### v1.0.0 (当前)
- ✅ 基于数据库的动态配置
- ✅ 多账号支持
- ✅ 优雅启动降级
- ✅ Redis 统一管理
- ✅ Bean 冲突修复
- ✅ 配置简化

### 下一步计划
- 🔄 配置热更新支持
- 🔄 Web 管理界面
- 🔄 更多消息类型支持

## 📝 开发指南

### 添加新功能
1. 在对应的 Service 中实现业务逻辑
2. 在 Controller 中暴露 API（可选）
3. 更新文档

### 修改配置
1. 修改数据库表结构（通过 Flyway）
2. 更新 Properties 类
3. 更新 ConfigService 加载逻辑
4. 更新文档

### 问题排查
1. 检查日志输出
2. 确认配置正确
3. 查看相关文档
4. 提交 Issue

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交规范
- feat: 新功能
- fix: 修复
- docs: 文档
- style: 格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

## 📧 联系方式

- Issue: [GitHub Issues](https://github.com/rymcu/mortise/issues)
- 文档: [docs/](.)

## ⚖️ 许可证

MIT License
