# 自定义监控指标集成指南

## 📋 概述

基于 `Resilience4jRateLimiterHealthIndicator` 的设计模式，为各个业务模块创建自定义监控指标的完整指南。

## 🏗️ 核心设计模式分析

### Resilience4jRateLimiterHealthIndicator 特点：

1. **实现 HealthIndicator 接口**：集成到 Spring Boot Actuator
2. **使用 record 类型**：简洁的依赖注入
3. **详细的指标信息**：使用 `withDetail()` 添加监控数据
4. **异常处理**：完整的错误处理和降级
5. **日志记录**：使用 `@Slf4j` 记录调试信息

```java
@Slf4j
public record Resilience4jRateLimiterHealthIndicator(
        RateLimiterRegistry rateLimiterRegistry) implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // 收集指标数据
            // 构建健康状态
            // 返回详细信息
        } catch (Exception e) {
            // 异常处理
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
```

## 🎯 各模块监控指标规划

### 1. 认证模块 (mortise-auth)
**监控重点**：
- ✅ JWT Token 活跃数量
- ✅ 用户会话状态
- ✅ Token 清理状态
- ✅ Redis 连接健康状态

**集成方式**：
```java
// 在 mortise-auth 模块中创建
@Component
public class JwtTokenHealthIndicator implements HealthIndicator {
    // 监控 JWT Token 和用户会话状态
}
```

### 2. 缓存模块 (mortise-cache)
**监控重点**：
- ✅ 缓存命中率
- ✅ 缓存大小统计
- ✅ 缓存清理状态
- ✅ 缓存管理器状态

**集成方式**：
```java
// 在 mortise-cache 模块中创建
@Component
public class CachePerformanceHealthIndicator implements HealthIndicator {
    // 监控缓存性能和状态
}
```

### 3. 通知模块 (mortise-notification)
**监控重点**：
- ✅ 消息发送成功率
- ✅ 消息队列积压情况
- ✅ 平均处理时间
- ✅ 消息处理状态

**集成方式**：
```java
// 在 mortise-notification 模块中创建
@Component
public class NotificationHealthIndicator implements HealthIndicator {
    // 监控通知服务状态
}
```

### 4. 系统模块 (mortise-system)
**监控重点**：
- ✅ 用户登录成功率
- ✅ 在线用户统计
- ✅ 新用户注册量
- ✅ 系统业务负载

**集成方式**：
```java
// 在 mortise-system 模块中创建
@Component
public class SystemBusinessHealthIndicator implements HealthIndicator {
    // 监控系统业务指标
}
```

### 5. 日志模块 (mortise-log)
**监控重点**：
- ✅ 各级别日志数量统计
- ✅ 错误日志比率
- ✅ 日志队列状态
- ✅ 日志系统活跃度

**集成方式**：
```java
// 在 mortise-log 模块中创建
@Component
public class LogSystemHealthIndicator implements HealthIndicator {
    // 监控日志系统状态
}
```

## 🛠️ 实际集成步骤

### 步骤 1：在目标模块中创建健康检查类

以认证模块为例，在 `mortise-auth` 中创建：

```bash
mortise-auth/src/main/java/com/rymcu/mortise/auth/health/JwtTokenHealthIndicator.java
```

### 步骤 2：确保模块依赖

确保目标模块的 `pom.xml` 包含必要依赖：

```xml
<!-- Spring Boot Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
    <optional>true</optional>
</dependency>

<!-- Micrometer (if using custom metrics) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <optional>true</optional>
</dependency>
```

### 步骤 3：注册为 Spring Bean

使用 `@Component` 注解自动注册：

```java
@Slf4j
@Component
public class CustomHealthIndicator implements HealthIndicator {
    // 实现逻辑
}
```

### 步骤 4：集成到业务逻辑

在相关业务代码中调用指标记录方法：

```java
@Service
public class AuthService {
    
    @Autowired
    private JwtTokenHealthIndicator healthIndicator;
    
    public void login(String username, String password) {
        try {
            // 登录逻辑
            healthIndicator.recordLoginSuccess();
        } catch (Exception e) {
            healthIndicator.recordLoginFailure();
            throw e;
        }
    }
}
```

## 📊 监控指标访问

### 健康检查端点
```bash
GET /actuator/health
```

**响应示例**：
```json
{
  "status": "UP",
  "components": {
    "jwtTokenHealthIndicator": {
      "status": "UP",
      "details": {
        "activeTokens": 156,
        "activeSessions": 89,
        "cleanupStatus": "正常",
        "redisConnection": "UP"
      }
    },
    "cachePerformanceHealthIndicator": {
      "status": "UP",
      "details": {
        "hitRate": "87.5%",
        "totalRequests": 2045,
        "cacheCount": 8,
        "performance": "良好"
      }
    }
  }
}
```

### Prometheus 指标端点
```bash
GET /actuator/prometheus
```

**响应示例**：
```
# HELP auth_tokens_active  
# TYPE auth_tokens_active gauge
auth_tokens_active 156.0

# HELP cache_hit_rate  
# TYPE cache_hit_rate gauge
cache_hit_rate 87.5

# HELP notification_success_rate  
# TYPE notification_success_rate gauge
notification_success_rate 98.2
```

## ⚡ 最佳实践

### 1. 性能考虑
- ✅ 使用原子类型 (`AtomicLong`) 进行线程安全计数
- ✅ 避免在健康检查中执行耗时操作
- ✅ 使用 `@Lazy` 注解避免循环依赖

### 2. 异常处理
- ✅ 完整的 try-catch 块
- ✅ 降级到 `Health.down()` 状态
- ✅ 记录错误详情和异常类型

### 3. 数据重置
- ✅ 使用 `@Scheduled` 定时重置统计数据
- ✅ 避免计数器无限增长
- ✅ 记录重置操作的日志

### 4. 指标命名
- ✅ 使用有意义的指标名称
- ✅ 添加模块前缀（如 `auth.`, `cache.`）
- ✅ 保持命名一致性

### 5. 健康状态判断
- ✅ 设置合理的阈值
- ✅ 提供清晰的状态描述
- ✅ 支持多级健康状态

## 🔄 与现有监控的集成

### 与 mortise-monitor 模块协同
- **DatabasePerformanceConfig** - 数据库层面监控
- **ApplicationPerformanceConfig** - 应用层面监控
- **自定义业务指标** - 业务层面监控

### 统一的监控体系
```
┌─────────────────┐
│   Grafana       │ ← 可视化展示
└─────────────────┘
          ↑
┌─────────────────┐
│   Prometheus    │ ← 指标收集
└─────────────────┘
          ↑
┌─────────────────┐
│   Actuator      │ ← 指标暴露
└─────────────────┘
          ↑
┌─────────────────┐
│  Custom Health  │ ← 业务指标
│   Indicators    │
└─────────────────┘
```

## 📁 文件结构示例

```
mortise/
├── mortise-auth/
│   └── src/main/java/.../auth/health/
│       └── JwtTokenHealthIndicator.java
├── mortise-cache/
│   └── src/main/java/.../cache/health/
│       └── CachePerformanceHealthIndicator.java
├── mortise-notification/
│   └── src/main/java/.../notification/health/
│       └── NotificationHealthIndicator.java
├── mortise-system/
│   └── src/main/java/.../system/health/
│       └── SystemBusinessHealthIndicator.java
├── mortise-log/
│   └── src/main/java/.../log/health/
│       └── LogSystemHealthIndicator.java
└── mortise-monitor/
    └── src/main/java/.../monitor/config/
        ├── ApplicationPerformanceConfig.java
        ├── DatabasePerformanceConfig.java
        └── ApplicationStartupConfig.java
```

## 🚀 快速开始

1. **选择目标模块**：确定要添加监控的模块
2. **复制示例代码**：从对应的示例文件复制代码
3. **调整业务逻辑**：根据实际业务需求修改指标收集
4. **集成到服务中**：在业务代码中调用指标记录方法
5. **测试验证**：访问 `/actuator/health` 验证指标生效
6. **配置告警**：基于指标设置监控告警

通过这种方式，每个模块都可以拥有自己专业的监控指标，形成完整的系统监控体系！