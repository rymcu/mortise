# Resilience4j RateLimiter 健康监控集成完成

## ✅ 集成状态

`Resilience4jRateLimiterHealthIndicator` 已成功集成到 **`mortise-web` 模块**中！

### 📁 文件位置
```
mortise-web/
└── src/main/java/com/rymcu/mortise/web/
    └── health/
        └── Resilience4jRateLimiterHealthIndicator.java
```

## 🎯 为什么放在 mortise-web？

### 方案 A（已采用）：放在 mortise-web 模块 ✅

**核心原因**：
1. **业务逻辑就在这里**：限流器的使用逻辑在 `RateLimitAspect`（mortise-web）
2. **模块内聚性**：监控和业务逻辑在同一模块，便于维护
3. **依赖关系清晰**：避免 `mortise-monitor` 依赖 `mortise-web`

**架构图**：
```
mortise-web
├── aspect/
│   └── RateLimitAspect.java           ← 限流逻辑
├── annotation/
│   └── RateLimit.java                 ← 限流注解
└── health/
    └── Resilience4jRateLimiterHealthIndicator.java  ← 限流监控
```

### 方案 B（未采用）：放在 mortise-monitor 模块 ❌

**不采用的原因**：
- 会导致 `mortise-monitor` 需要依赖 `mortise-web`
- 违反了"基础设施监控"和"业务监控"的分离原则
- 限流器是 Web 层的业务功能，不是纯基础设施

## 📦 依赖配置

### mortise-web/pom.xml
```xml
<!-- Resilience4j (限流) -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-ratelimiter</artifactId>
</dependency>

<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>

<!-- Spring Boot Actuator (健康检查) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
    <optional>true</optional>
</dependency>
```

**说明**：
- ✅ `resilience4j-ratelimiter`：限流核心库
- ✅ `resilience4j-spring-boot3`：Spring Boot 3 集成
- ✅ `actuator`：健康检查（设为 `optional`，不强制依赖）

## 🔧 核心实现

### Resilience4jRateLimiterHealthIndicator.java

```java
@Component
@ConditionalOnClass(RateLimiterRegistry.class)
public class Resilience4jRateLimiterHealthIndicator implements HealthIndicator {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    public Resilience4jRateLimiterHealthIndicator(Optional<RateLimiterRegistry> registryOptional) {
        this.rateLimiterRegistry = registryOptional.orElse(null);
    }

    @Override
    public Health health() {
        // 检查所有 RateLimiter 的状态
        // - availablePermissions: 可用许可数
        // - waitingThreads: 等待线程数
        // - status: HEALTHY / ACTIVE / EXHAUSTED
    }
}
```

### 关键特性

1. **可选依赖处理**：使用 `Optional<RateLimiterRegistry>` 优雅处理依赖缺失
2. **条件加载**：`@ConditionalOnClass` 确保只在 Resilience4j 存在时加载
3. **详细指标**：监控每个限流器的可用许可、等待线程、配置信息
4. **健康状态**：
   - ✅ `HEALTHY`：所有限流器运行正常
   - ⚠️ `ACTIVE`：有线程正在等待许可（正常限流中）
   - ❌ `EXHAUSTED`：限流器已耗尽（需要关注）

## 📊 监控指标

### 访问健康检查端点

```bash
# 查看所有健康检查
curl http://localhost:8080/actuator/health

# 查看 RateLimiter 详细状态
curl http://localhost:8080/actuator/health/rateLimiter
```

### 响应示例

```json
{
  "status": "UP",
  "components": {
    "rateLimiter": {
      "status": "UP",
      "details": {
        "rateLimiter.api-limiter": {
          "limitForPeriod": "配置正常",
          "limitRefreshPeriod": "PT1S",
          "timeoutDuration": "PT5S",
          "availablePermissions": 10,
          "waitingThreads": 0,
          "status": "HEALTHY - 运行正常"
        },
        "summary": {
          "totalRateLimiters": 1,
          "activeRateLimiters": 0,
          "registryStatus": "已配置"
        },
        "message": "所有 RateLimiter 运行正常"
      }
    }
  }
}
```

## 🚀 使用示例

### 1. 配置限流器

```yaml
# application.yml
resilience4j:
  ratelimiter:
    configs:
      default:
        limit-for-period: 10      # 每个周期允许 10 个请求
        limit-refresh-period: 1s  # 刷新周期 1 秒
        timeout-duration: 5s      # 等待超时 5 秒
    instances:
      api-limiter:
        base-config: default
      admin-limiter:
        limit-for-period: 5
        limit-refresh-period: 1s
```

### 2. 使用限流注解

```java
@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/data")
    @RateLimit(name = "api-limiter", message = "请求过于频繁，请稍后再试")
    public String getData() {
        return "data";
    }
}
```

### 3. 监控限流状态

- 健康检查会自动监控所有已配置的 RateLimiter
- 当限流器耗尽时，健康状态会变为 `DOWN`
- 可以集成到监控系统（Prometheus、Grafana 等）

## 📋 配置选项

### 启用/禁用健康检查

```yaml
management:
  health:
    rateLimiter:
      enabled: true  # 启用 RateLimiter 健康检查
  endpoint:
    health:
      show-details: always  # 显示详细信息
```

## 🎯 架构原则总结

### ✅ 正确的模块划分

| 监控类型 | 放置位置 | 示例 |
|---------|---------|------|
| **Web 层功能监控** | `mortise-web` | RateLimiter、请求统计、API 状态 |
| **认证功能监控** | `mortise-auth` | JWT Token、OAuth2、登录统计 |
| **系统业务监控** | `mortise-system` | 用户在线、业务指标 |
| **基础设施监控** | `mortise-monitor` | JVM、数据库连接池、Redis |

### 💡 核心原则

> **"谁拥有业务，谁拥有监控"**

- 业务功能的监控放在业务模块
- 基础设施的监控放在 `mortise-monitor`
- 避免所有监控都集中在 `mortise-monitor`

## 🔗 相关文档

- [监控指标放置位置快速决策表](./monitoring-placement-quick-reference.md)
- [自定义监控指标指南](./CUSTOM_MONITORING_GUIDE.md)
- [限流功能说明](./rate-limiting.md)

## ✅ 验证清单

- [x] `Resilience4jRateLimiterHealthIndicator` 已创建在 `mortise-web` 模块
- [x] `actuator` 依赖已添加到 `mortise-web/pom.xml`
- [x] 使用 `Optional` 处理可选依赖
- [x] 使用 `@ConditionalOnClass` 条件加载
- [x] 编译通过：`mvn clean compile -pl mortise-web -am -q`
- [x] 架构原则文档已更新

## 🎉 总结

`Resilience4jRateLimiterHealthIndicator` 现在已经：

1. ✅ 正确放置在 `mortise-web` 模块中
2. ✅ 与限流业务逻辑（`RateLimitAspect`）在同一模块
3. ✅ 遵循"谁拥有业务，谁拥有监控"的架构原则
4. ✅ 提供详细的限流器状态监控
5. ✅ 可以通过 `/actuator/health` 端点访问

**这是正确的架构设计！** 🎯
