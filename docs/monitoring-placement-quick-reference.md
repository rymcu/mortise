# 监控指标放置位置快速决策表

## 🎯 快速决策

| 监控内容 | 应该放在哪里 | 原因 |
|---------|------------|------|
| JWT Token 统计 | `mortise-auth` | 认证业务逻辑在此模块 |
| OAuth2 登录状态 | `mortise-auth` | 认证业务逻辑在此模块 |
| 缓存命中率 | `mortise-cache` | 缓存业务逻辑在此模块 |
| 缓存失效处理 | `mortise-cache` | 缓存业务逻辑在此模块 |
| 用户在线统计 | `mortise-system` | 用户业务逻辑在此模块 |
| 登录成功率 | `mortise-system` | 用户业务逻辑在此模块 |
| API 限流状态 | `mortise-web` | 限流功能在此模块 |
| 请求统计 | `mortise-web` | Web 功能在此模块 |
| 消息发送统计 | `mortise-notification` | 通知业务逻辑在此模块 |
| 消息队列积压 | `mortise-notification` | 通知业务逻辑在此模块 |
| 错误日志率 | `mortise-log` | 日志业务逻辑在此模块 |
| 日志队列状态 | `mortise-log` | 日志业务逻辑在此模块 |
| **JVM 内存监控** | **mortise-monitor** | **基础设施，与业务无关** |
| **数据库连接池** | **mortise-monitor** | **基础设施，与业务无关** |
| **应用启动信息** | **mortise-monitor** | **基础设施，与业务无关** |
| **Redis 连接状态** | **mortise-monitor** | **基础设施，与业务无关** |

## ⚡ 一句话判断

**"这个监控是为了监控业务功能，还是为了监控基础设施？"**

- **业务功能** → 放在对应的业务模块
- **基础设施** → 放在 `mortise-monitor`

## 📋 常见场景

### 场景 1：新增订单系统，需要监控订单处理状态
**答案**：在 `mortise-order` 模块中创建 `OrderHealthIndicator`
**原因**：订单是业务功能

### 场景 2：需要监控 Redis 缓存的内存使用情况
**答案**：在 `mortise-monitor` 模块中创建或扩展现有的 `RedisHealthIndicator`
**原因**：Redis 连接是基础设施

### 场景 3：需要监控某个特定业务的缓存命中率
**答案**：在对应的业务模块中创建监控
**例如**：用户缓存命中率 → `mortise-system` 的 `UserCacheHealthIndicator`
**原因**：虽然涉及缓存，但是业务相关的指标

### 场景 4：需要监控 Resilience4j 限流器状态
**答案**：✅ 在 `mortise-web` 模块中创建 `Resilience4jRateLimiterHealthIndicator`
**原因**：
- 限流逻辑（`RateLimitAspect`）在 `mortise-web` 模块
- 限流是 Web 层的业务功能，不是纯基础设施
- 遵循"谁拥有业务，谁拥有监控"原则

## 🚫 反模式（不要这样做）

### ❌ 错误做法 1：所有监控都放在 mortise-monitor
```
mortise-monitor/
└── health/
    ├── JwtTokenHealthIndicator.java        ← ❌ 应该在 mortise-auth
    ├── OrderHealthIndicator.java           ← ❌ 应该在 mortise-order
    ├── CacheHealthIndicator.java           ← ❌ 应该在 mortise-cache
    └── NotificationHealthIndicator.java    ← ❌ 应该在 mortise-notification
```

**问题**：
- `mortise-monitor` 需要依赖所有业务模块
- 造成循环依赖
- 违反模块分层原则
- 难以维护

### ❌ 错误做法 2：业务模块监控基础设施
```
mortise-system/
└── health/
    ├── JVMHealthIndicator.java             ← ❌ 应该在 mortise-monitor
    └── DatabasePoolHealthIndicator.java    ← ❌ 应该在 mortise-monitor
```

**问题**：
- 基础设施监控分散
- 重复代码
- 难以统一管理

## ✅ 正确示例

### 示例 1：用户模块监控（业务监控）
```java
// 位置：mortise-system/src/main/java/com/rymcu/mortise/system/health/
@Component
public class UserActivityHealthIndicator implements HealthIndicator {
    
    @Autowired
    private UserService userService;  // ← 使用本模块的 Service
    
    @Override
    public Health health() {
        long onlineUsers = userService.getOnlineUserCount();
        // 监控业务指标
    }
}
```

### 示例 2：基础设施监控
```java
// 位置：mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/
@Configuration
public class DatabasePerformanceConfig {
    
    @Autowired
    private DataSource dataSource;  // ← 监控基础设施组件
    
    // 监控数据库连接池
}
```

## 🔧 实施步骤

### 为业务模块添加监控

1. **在业务模块中创建 health 包**
   ```
   mortise-xxx/src/main/java/com/rymcu/mortise/xxx/health/
   ```

2. **创建 HealthIndicator 类**
   ```java
   @Component
   public class XxxHealthIndicator implements HealthIndicator {
       // 实现监控逻辑
   }
   ```

3. **添加必要依赖（如果没有）**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
       <optional>true</optional>
   </dependency>
   ```

4. **在业务代码中记录指标**
   ```java
   @Service
   public class XxxService {
       @Autowired
       private XxxHealthIndicator healthIndicator;
       
       public void doSomething() {
           // 业务逻辑
           healthIndicator.recordMetric();
       }
   }
   ```

## 📊 模块依赖关系

```
┌─────────────────────┐
│   mortise-monitor   │  ← 监控基础设施，不依赖业务模块
│  (基础设施监控)      │
└─────────────────────┘
          ↑
          │ 可选依赖（optional）
          │
┌─────────────────────┐
│  mortise-auth       │  ← 监控认证业务
│  mortise-cache      │  ← 监控缓存业务
│  mortise-system     │  ← 监控系统业务
│  mortise-web        │  ← 监控 Web 业务
│  mortise-xxx        │  ← 监控其他业务
└─────────────────────┘
  (业务模块，各自监控各自的业务)
```

**关键点**：
- `mortise-monitor` 不依赖业务模块
- 业务模块可以可选依赖 `actuator`
- 各模块监控独立，互不影响