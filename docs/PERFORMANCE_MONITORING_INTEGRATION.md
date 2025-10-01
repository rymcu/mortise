# 性能监控配置集成说明

## 概述

成功将三个性能监控配置文件集成到 `mortise-monitor` 模块中，增强了系统的监控能力。

## 集成的配置

### 1. ApplicationStartupConfig.java
**位置**：`mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/ApplicationStartupConfig.java`

**功能**：
- ✅ 记录应用启动时间
- ✅ 输出运行环境信息（Java版本、JVM、操作系统、CPU、内存等）
- ✅ 格式化的启动日志输出

**实现方式**：
- 监听 `ApplicationReadyEvent` 事件
- 通过 `System.getProperty("app.start.time")` 获取启动时间
- 使用 `Runtime` API 获取系统信息

### 2. ApplicationPerformanceConfig.java
**位置**：`mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/ApplicationPerformanceConfig.java`

**功能**：
- ✅ JVM 性能健康检查指示器
- ✅ 堆内存使用率监控（每分钟）
- ✅ 线程数量监控
- ✅ 性能告警（内存使用率 > 80% 或线程数 > 200）
- ✅ 定时输出性能状态日志（每10分钟）

**监控指标**：
- `application.memory.heap.usage.percent` - 堆内存使用率
- `application.threads.current` - 当前线程数
- `application.threads.peak` - 峰值线程数

**健康检查端点**：
- `/actuator/health` 中包含 `jvmPerformanceHealthIndicator`
- 显示堆内存、非堆内存、线程数等详细信息

### 3. DatabasePerformanceConfig.java
**位置**：`mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/DatabasePerformanceConfig.java`

**功能**：
- ✅ HikariCP 连接池健康检查
- ✅ 连接池性能指标收集（每30秒）
- ✅ 连接池使用率监控和告警（使用率 > 80%）
- ✅ 等待连接线程监控
- ✅ 定时输出连接池状态（每5分钟）

**监控指标**：
- `hikari.connections.active` - 活跃连接数
- `hikari.connections.idle` - 空闲连接数
- `hikari.connections.total` - 总连接数
- `hikari.connections.max` - 最大连接数
- `hikari.connections.min` - 最小连接数
- `hikari.threads.awaiting` - 等待连接的线程数
- `hikari.connections.usage` - 连接池使用率

**健康检查端点**：
- `/actuator/health` 中包含 `hikariHealthIndicator`
- 显示连接池名称、活跃/空闲/总连接数、等待线程数等

## 依赖添加

在 `mortise-monitor/pom.xml` 中添加了可选依赖：

```xml
<!-- Spring JDBC (for HikariCP monitoring) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
    <optional>true</optional>
</dependency>
```

**说明**：使用 `optional=true` 表示这是可选依赖，只有在项目中使用了数据库时才会生效。

## 监控特性

### 自动告警
- **内存告警**：当堆内存使用率 > 80% 时记录警告日志
- **线程告警**：当线程数 > 200 时记录警告日志
- **连接池告警**：当连接池使用率 > 80% 时记录警告日志
- **等待连接告警**：当有线程等待数据库连接时记录警告日志

### 定时任务
- **性能指标收集**：每分钟收集一次 JVM 性能指标
- **连接池指标收集**：每30秒收集一次连接池指标
- **性能状态日志**：每10分钟输出一次应用性能状态
- **连接池状态日志**：每5分钟输出一次连接池状态

### 循环依赖处理
所有配置类都使用 `@Lazy` 注解注入 `MeterRegistry` 和 `DataSource`，避免循环依赖问题。

## 健康检查端点

访问 `/actuator/health` 可以看到：

```json
{
  "status": "UP",
  "components": {
    "jvmPerformanceHealthIndicator": {
      "status": "UP",
      "details": {
        "memory.heap.used": "256.5MB",
        "memory.heap.max": "1.0GB",
        "memory.heap.usage": "25.05%",
        "threads.current": 45,
        "threads.peak": 52,
        "performance.status": "良好"
      }
    },
    "hikariHealthIndicator": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "poolName": "HikariPool-1",
        "activeConnections": 2,
        "totalConnections": 10,
        "idleConnections": 8,
        "threadsAwaitingConnection": 0,
        "maxPoolSize": 10,
        "minIdle": 2
      }
    }
  }
}
```

## Prometheus 指标

访问 `/actuator/prometheus` 可以获取所有监控指标：

```
# HELP application_memory_heap_usage_percent  
# TYPE application_memory_heap_usage_percent gauge
application_memory_heap_usage_percent 25.05

# HELP application_threads_current  
# TYPE application_threads_current gauge
application_threads_current 45.0

# HELP hikari_connections_usage  
# TYPE hikari_connections_usage gauge
hikari_connections_usage 0.2

# HELP hikari_connections_active  
# TYPE hikari_connections_active gauge
hikari_connections_active 2.0
```

## 启动日志示例

```
✅ 应用启动完成！总耗时: 8523 ms (8.52 s)
════════════════════════════════════════════════════════
运行环境信息:
  Java 版本: 21.0.1
  JVM 厂商: Oracle Corporation
  JVM 版本: OpenJDK 64-Bit Server VM
  操作系统: Windows 11 10.0 amd64
  CPU 核心数: 12
  内存信息:
    最大内存: 4096 MB
    总内存: 512 MB
    已用内存: 256 MB
    空闲内存: 256 MB
  工作目录: D:\rymcu2024\mortise
  Java 类路径: D:\rymcu2024\mortise\mortise-app\target\classes;...
════════════════════════════════════════════════════════
```

## 性能监控日志示例

```
📊 应用性能状态 - 堆内存: 256.5MB/1.0GB (25.1%), 非堆内存: 89.2MB/-1B, 线程数: 45 (峰值: 52, 守护: 38)

💾 数据库连接池状态 [HikariPool-1] - 活跃: 2, 空闲: 8, 总计: 10/10, 等待: 0, 最小空闲: 2, 使用率: 20.0%
```

## 配置要求

### 启动时间记录（可选）

如果要记录应用启动时间，需要在启动脚本或主类中设置系统属性：

```java
public static void main(String[] args) {
    System.setProperty("app.start.time", String.valueOf(System.currentTimeMillis()));
    SpringApplication.run(Application.class, args);
}
```

或在启动命令中：
```bash
java -Dapp.start.time=$(date +%s%3N) -jar mortise.war
```

### 启用监控端点

在 `application.yml` 中配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

## 验证结果

- ✅ 编译成功：`mvn clean compile -pl mortise-monitor -am -q`
- ✅ 依赖正确：添加了可选的 JDBC 依赖
- ✅ 无循环依赖：使用 `@Lazy` 注解避免循环依赖
- ✅ 条件化配置：使用 `@ConditionalOnClass` 确保只在相应类存在时生效

## 与现有配置的关系

### 增强现有功能
- **扩展** `DatabaseHealthIndicator` → 提供更详细的连接池监控
- **扩展** `MetricsConfig` → 添加应用性能和数据库性能指标
- **新增** 启动信息记录功能

### 不冲突
- 所有配置使用不同的 Bean 名称
- 健康检查指示器使用不同的名称
- 指标使用不同的前缀

## 下一步建议

1. **配置启动时间记录**：在主应用类中添加启动时间记录
2. **调整告警阈值**：根据实际情况调整内存、线程、连接池的告警阈值
3. **集成 Grafana**：使用 Prometheus + Grafana 可视化监控指标
4. **添加更多指标**：根据业务需要添加自定义监控指标
5. **配置告警通知**：集成告警通知系统（如钉钉、企业微信）