# Resilience4j 限流方案

本项目采用 **Resilience4j** 作为唯一的限流解决方案，提供现代化、功能丰富的限流保护。

## 🎯 方案特点

### ✅ 优势
- **现代化设计**: 专为Java 8+和Spring Boot设计
- **丰富监控**: 完整的Micrometer指标集成，支持Prometheus监控
- **优雅降级**: 内置fallback机制，限流触发时可执行降级逻辑
- **灵活配置**: 支持动态配置和多种限流策略
- **Spring集成**: 与Spring Boot完美融合
- **函数式编程**: 支持装饰器模式和函数式编程

### 📊 性能表现
- **吞吐量**: 5,000-20,000 req/s
- **延迟**: < 1ms
- **内存占用**: 中等
- **CPU占用**: 低-中等

## 🚀 快速使用

### 1. 基础限流
```java
@Resilience4jRateLimit(
    limitForPeriod = 10,          // 每个周期允许10个请求
    refreshPeriodSeconds = 1,     // 1秒刷新周期
    timeoutMillis = 100,          // 100ms超时
    message = "请求过于频繁，请稍后再试"
)
public Result someAPI() {
    // 业务逻辑
}
```

### 2. 带降级处理
```java
@Resilience4jRateLimit(
    limitForPeriod = 5,
    refreshPeriodSeconds = 1,
    enableFallback = true,        // 启用降级
    fallbackMethod = "fallbackMethod",
    message = "系统繁忙，已切换到降级处理"
)
public Result importantAPI() {
    // 重要业务逻辑
}

// 降级方法
public Result fallbackMethod() {
    return Result.success("降级处理结果");
}
```

### 3. 自定义Key策略
```java
@Resilience4jRateLimit(
    keyType = Resilience4jRateLimit.KeyType.CUSTOM,
    keyExpression = "#userId + ':' + #ip",  // SpEL表达式
    limitForPeriod = 20,
    message = "用户请求频率超限"
)
public Result userAPI(@RequestParam String userId) {
    // 基于用户和IP的限流
}
```

## 🎛️ 配置说明

### application.yml 配置
```yaml
app:
  rate-limit:
    admin:
      enabled: true                    # 启用管理后台
    performance-test:
      enabled: true                    # 启用性能测试
    resilience4j:
      enabled: true                    # 启用Resilience4j限流
      health-check-enabled: true       # 启用健康检查
      default-config:
        limit-for-period: 10           # 默认限制数
        refresh-period-seconds: 1      # 默认周期
        timeout-millis: 100            # 默认超时
      configs:
        strict:                        # 严格配置
          limit-for-period: 5
          refresh-period-seconds: 1
          timeout-millis: 50
        loose:                         # 宽松配置
          limit-for-period: 50
          refresh-period-seconds: 1
          timeout-millis: 200
```

## 📱 管理和监控

### 管理接口
- `GET /api/v1/admin/rate-limit/status` - 获取限流器状态
- `GET /api/v1/admin/rate-limit/resilience4j/status/{key}` - 获取指定限流器状态
- `DELETE /api/v1/admin/rate-limit/resilience4j/remove/{key}` - 移除限流器
- `POST /api/v1/admin/rate-limit/test` - 测试限流功能
- `GET /api/v1/admin/rate-limit/info` - 获取详细信息

### 性能测试
- `POST /api/v1/admin/rate-limit/performance/single-thread` - 单线程性能测试
- `POST /api/v1/admin/rate-limit/performance/multi-thread` - 多线程性能测试
- `POST /api/v1/admin/rate-limit/performance/comprehensive` - 全面性能测试

### 演示接口
- `GET /api/v1/demo/rate-limit/basic` - 基础限流演示
- `GET /api/v1/demo/rate-limit/with-fallback` - 降级处理演示
- `GET /api/v1/demo/rate-limit/custom-key` - 自定义Key演示
- `GET /api/v1/demo/rate-limit/user-based` - 基于用户限流
- `POST /api/v1/demo/rate-limit/test-all` - 功能展示

## 📊 监控集成

### Actuator健康检查
访问 `/actuator/health` 可以看到限流器的健康状态：
```json
{
  "status": "UP",
  "components": {
    "resilience4jRateLimiterHealthIndicator": {
      "status": "UP",
      "details": {
        "totalRateLimiters": 3,
        "rateLimiter.user-api.availablePermissions": 8,
        "rateLimiter.user-api.waitingThreads": 0
      }
    }
  }
}
```

### Prometheus指标
限流器会自动导出以下指标到Prometheus：
- `resilience4j_ratelimiter_calls_total` - 总调用次数
- `resilience4j_ratelimiter_available_permissions` - 可用许可数
- `resilience4j_ratelimiter_waiting_threads` - 等待线程数

### Grafana监控
可以使用以下查询来监控限流器：
```promql
# 限流器调用成功率
rate(resilience4j_ratelimiter_calls_total{kind="successful"}[5m])

# 限流器阻塞率  
rate(resilience4j_ratelimiter_calls_total{kind="failed"}[5m])

# 可用许可数
resilience4j_ratelimiter_available_permissions
```

## 🎨 使用场景

### 适用场景
- ✅ 现代Spring Boot应用
- ✅ 需要丰富监控的系统  
- ✅ 要求优雅降级处理
- ✅ 微服务架构
- ✅ 单体应用的重要接口保护

### 典型应用
```java
// 高频查询接口
@Resilience4jRateLimit(limitForPeriod = 1000, refreshPeriodSeconds = 1)
public Result searchAPI() { /* ... */ }

// 重要业务接口  
@Resilience4jRateLimit(
    limitForPeriod = 100,
    enableFallback = true,
    fallbackMethod = "businessFallback"
)
public Result businessAPI() { /* ... */ }

// 用户相关接口
@Resilience4jRateLimit(
    keyType = KeyType.USER_ID,
    limitForPeriod = 50,
    refreshPeriodSeconds = 10
)
public Result userAPI() { /* ... */ }
```

## ⚡ 性能优化

### 配置优化
1. **合理设置超时时间**: 根据业务需求调整`timeoutMillis`
2. **优化限流参数**: 根据实际QPS调整`limitForPeriod`和`refreshPeriodSeconds`  
3. **启用监控**: 配置Micrometer指标收集
4. **合理使用降级**: 为关键接口配置fallback方法

### 最佳实践
1. **分层限流**: 不同重要级别的接口使用不同的限流参数
2. **监控告警**: 配置限流指标的监控告警
3. **压测验证**: 使用性能测试接口验证配置是否合理
4. **渐进式部署**: 新配置先在测试环境验证

## 🔧 故障排查

### 常见问题
1. **限流过于严格**: 调整`limitForPeriod`参数
2. **响应延迟高**: 检查`timeoutMillis`设置和业务逻辑
3. **降级未生效**: 检查`fallbackMethod`方法签名是否正确
4. **监控数据缺失**: 确认Micrometer配置是否正确

### 调试技巧
1. 启用DEBUG日志查看限流详情
2. 使用管理接口查看限流器状态
3. 通过Actuator健康检查验证配置
4. 使用性能测试接口验证效果

## 📚 相关文档
- [Resilience4j官方文档](https://resilience4j.readme.io/)
- [Spring Boot Actuator文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer文档](https://micrometer.io/docs)
- [Prometheus监控文档](https://prometheus.io/docs/)

---

通过采用Resilience4j作为唯一限流方案，我们获得了现代化、功能丰富且易于维护的限流保护能力。🚀