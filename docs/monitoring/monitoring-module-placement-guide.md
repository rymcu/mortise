# 监控指标模块职责划分指南

## 🎯 核心原则

**谁的业务，谁负责监控！**

监控指标应该放在业务逻辑所在的模块中，而不是统一放在 `mortise-monitor` 模块。

## 📊 模块职责划分

### 1. mortise-monitor 模块
**职责**：基础设施层面的监控

✅ **应该放置**：
- 应用启动监控 (`ApplicationStartupConfig`)
- JVM 性能监控 (`ApplicationPerformanceConfig`)
- 数据库连接池监控 (`DatabasePerformanceConfig`)
- 通用的 Redis 健康检查 (`RedisHealthIndicator`)
- 通用的数据库健康检查 (`DatabaseHealthIndicator`)
- Actuator 端点配置
- Prometheus/Micrometer 基础配置

❌ **不应该放置**：
- 业务相关的监控指标
- 业务功能的健康检查
- 业务数据统计

**原因**：`mortise-monitor` 是基础设施模块，不应该依赖业务模块，否则会造成循环依赖。

---

### 2. 业务模块监控指标放置位置

#### mortise-auth 模块
**监控重点**：认证授权相关业务

```
mortise-auth/
└── src/main/java/com/rymcu/mortise/auth/
    ├── health/
    │   ├── JwtTokenHealthIndicator.java       ← JWT Token 监控
    │   └── OAuth2HealthIndicator.java         ← OAuth2 监控
    └── metrics/
        └── AuthMetricsCollector.java          ← 认证指标收集器
```

**监控内容**：
- JWT Token 活跃数量
- Token 过期清理状态
- OAuth2 登录成功率
- 用户会话统计
- 登录失败率

---

#### mortise-cache 模块
**监控重点**：缓存性能相关

```
mortise-cache/
└── src/main/java/com/rymcu/mortise/cache/
    ├── health/
    │   └── CachePerformanceHealthIndicator.java  ← 缓存性能监控
    └── metrics/
        └── CacheMetricsCollector.java            ← 缓存指标收集器
```

**监控内容**：
- 缓存命中率
- 缓存大小统计
- 缓存清理状态
- 缓存失效事件处理

---

#### mortise-system 模块
**监控重点**：系统业务指标

```
mortise-system/
└── src/main/java/com/rymcu/mortise/system/
    ├── health/
    │   ├── SystemBusinessHealthIndicator.java    ← 系统业务健康检查
    │   └── UserActivityHealthIndicator.java      ← 用户活跃度监控
    └── metrics/
        └── BusinessMetricsCollector.java         ← 业务指标收集器
```

**监控内容**：
- 用户在线统计
- 登录成功率
- 新用户注册量
- 系统负载状态
- 业务操作统计

---

#### mortise-web-support 模块
**监控重点**：Web 层面监控

```
mortise-web-support/
└── src/main/java/com/rymcu/mortise/web/
    ├── health/
    │   └── Resilience4jRateLimiterHealthIndicator.java  ← 限流器监控
    └── metrics/
        └── WebMetricsCollector.java                     ← Web 指标收集器
```

**监控内容**：
- API 限流状态
- 请求统计
- 响应时间
- 错误率统计

---

#### mortise-notification 模块
**监控重点**：通知服务监控

```
mortise-notification/
└── src/main/java/com/rymcu/mortise/notification/
    ├── health/
    │   └── NotificationHealthIndicator.java      ← 通知服务监控
    └── metrics/
        └── NotificationMetricsCollector.java     ← 通知指标收集器
```

**监控内容**：
- 消息发送成功率
- 消息队列积压
- 平均处理时间
- 发送渠道状态

---

#### mortise-log 模块
**监控重点**：日志系统监控

```
mortise-log/
└── src/main/java/com/rymcu/mortise/log/
    ├── health/
    │   └── LogSystemHealthIndicator.java         ← 日志系统监控
    └── metrics/
        └── LogMetricsCollector.java              ← 日志指标收集器
```

**监控内容**：
- 错误日志率
- 日志写入速度
- 日志队列状态
- 日志存储状态

---

## 🔄 Resilience4jRateLimiterHealthIndicator 的特殊情况

**问题**：为什么这个可以放在 `mortise-monitor`？

**答案**：这是一个**边界情况**，有两种合理的选择：

### 选项 1：放在 mortise-web-support 模块（推荐）
```
mortise-web-support/src/main/java/com/rymcu/mortise/web/health/
└── Resilience4jRateLimiterHealthIndicator.java
```

**理由**：
- RateLimiter 功能在 `mortise-web-support` 模块中实现
- 业务逻辑在哪里，监控就在哪里
- 符合模块职责划分原则

**需要做的**：
1. 在 `mortise-web-support/pom.xml` 添加可选的 actuator 依赖
2. 在 `mortise-web-support` 模块中创建 health 包
3. 实现 HealthIndicator

### 选项 2：放在 mortise-monitor 模块（可接受）
```
mortise-monitor/src/main/java/com/rymcu/mortise/monitor/health/
└── Resilience4jRateLimiterHealthIndicator.java
```

**理由**：
- Resilience4j 是基础设施组件（类似限流中间件）
- 不涉及具体业务逻辑
- 监控的是基础设施状态

**需要做的**：
1. 在 `mortise-monitor/pom.xml` 添加可选的 resilience4j 依赖
2. 使用 `@ConditionalOnClass` 确保只在存在时生效

---

## 📝 新业务监控指标添加示例

### 场景：添加订单系统监控

假设你新增了一个订单模块 `mortise-order`：

```
mortise-order/
└── src/main/java/com/rymcu/mortise/order/
    ├── service/
    │   └── OrderService.java              ← 订单业务逻辑
    ├── health/                            ← 👈 在这里添加监控
    │   └── OrderHealthIndicator.java      ← 订单健康检查
    └── metrics/
        └── OrderMetricsCollector.java     ← 订单指标收集
```

**示例代码**：
```java
// mortise-order/src/main/java/com/rymcu/mortise/order/health/OrderHealthIndicator.java
package com.rymcu.mortise.order.health;

@Slf4j
@Component
public class OrderHealthIndicator implements HealthIndicator {
    
    private final OrderService orderService;
    private final MeterRegistry meterRegistry;
    
    @Override
    public Health health() {
        try {
            // 获取订单统计
            long pendingOrders = orderService.getPendingOrderCount();
            long processingOrders = orderService.getProcessingOrderCount();
            double successRate = orderService.getOrderSuccessRate();
            
            // 记录指标
            meterRegistry.gauge("order.pending", pendingOrders);
            meterRegistry.gauge("order.processing", processingOrders);
            meterRegistry.gauge("order.success.rate", successRate);
            
            // 判断健康状态
            boolean isHealthy = successRate >= 95.0 && pendingOrders < 1000;
            
            return (isHealthy ? Health.up() : Health.down())
                    .withDetail("pendingOrders", pendingOrders)
                    .withDetail("processingOrders", processingOrders)
                    .withDetail("successRate", String.format("%.2f%%", successRate))
                    .build();
        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
```

---

## 🏗️ 依赖配置

### 业务模块需要添加的依赖

在业务模块的 `pom.xml` 中添加（如果还没有）：

```xml
<!-- Spring Boot Actuator (可选依赖) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
    <optional>true</optional>
</dependency>

<!-- Micrometer (如果需要自定义指标) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <optional>true</optional>
</dependency>
```

**注意**：使用 `<optional>true</optional>` 确保：
- 不是强制依赖
- 只在需要监控功能时启用
- 不会污染其他模块

---

## 🎯 决策流程图

```
新增监控指标
    ↓
问：这是业务功能的监控吗？
    ↓
是 → 放在对应的业务模块
│   例如：
│   - 用户相关 → mortise-system
│   - 认证相关 → mortise-auth
│   - 缓存相关 → mortise-cache
│   - 订单相关 → mortise-order
│
否 → 问：这是基础设施监控吗？
    ↓
    是 → 放在 mortise-monitor
    │   例如：
    │   - JVM 监控
    │   - 数据库连接池
    │   - Redis 连接
    │   - 应用启动信息
    │
    否 → 重新评估，可能需要新建模块
```

---

## ✅ 总结

| 监控类型 | 放置位置 | 示例 |
|---------|---------|------|
| 基础设施监控 | `mortise-monitor` | JVM、数据库连接池、应用启动 |
| 认证授权监控 | `mortise-auth` | JWT Token、OAuth2、登录统计 |
| 缓存性能监控 | `mortise-cache` | 命中率、失效处理 |
| 系统业务监控 | `mortise-system` | 用户活跃、业务统计 |
| Web 层监控 | `mortise-web-support` | 限流器、请求统计 |
| 通知服务监控 | `mortise-notification` | 消息发送、队列状态 |
| 日志系统监控 | `mortise-log` | 错误率、日志队列 |
| **新业务监控** | **新业务模块** | **订单、支付等** |

**核心原则**：
1. ✅ 业务监控放在业务模块
2. ✅ 基础设施监控放在 monitor 模块
3. ✅ 避免 monitor 模块依赖业务模块
4. ✅ 使用 optional 依赖避免污染