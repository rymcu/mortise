# 性能监控配置快速参考

## 📦 已集成配置文件

| 配置文件 | 模块位置 | 主要功能 | 状态 |
|---------|---------|---------|------|
| `ApplicationStartupConfig` | `mortise-monitor` | 启动时间和环境信息 | ✅ 已集成 |
| `ApplicationPerformanceConfig` | `mortise-monitor` | JVM性能监控 | ✅ 已集成 |
| `DatabasePerformanceConfig` | `mortise-monitor` | 数据库连接池监控 | ✅ 已集成 |

## 🎯 监控指标一览

### JVM 性能指标
```
application.memory.heap.usage.percent  # 堆内存使用率
application.threads.current            # 当前线程数
application.threads.peak              # 峰值线程数
```

### 数据库连接池指标
```
hikari.connections.active    # 活跃连接数
hikari.connections.idle      # 空闲连接数
hikari.connections.total     # 总连接数
hikari.connections.usage     # 使用率
hikari.threads.awaiting      # 等待连接的线程数
```

## 🔔 自动告警阈值

| 指标 | 阈值 | 日志级别 |
|-----|------|---------|
| 堆内存使用率 | > 80% | WARN |
| 线程数量 | > 200 | WARN |
| 连接池使用率 | > 80% | WARN |
| 等待连接线程 | > 0 | WARN |

## 🔍 监控端点

| 端点 | 路径 | 说明 |
|-----|------|------|
| 健康检查 | `/actuator/health` | 查看所有健康指标 |
| Prometheus | `/actuator/prometheus` | 导出 Prometheus 指标 |
| 指标详情 | `/actuator/metrics` | 查看所有可用指标 |

## ⏰ 定时任务

| 任务 | 频率 | 说明 |
|-----|------|------|
| JVM性能指标收集 | 每分钟 | 收集内存、线程指标 |
| 连接池指标收集 | 每30秒 | 收集连接池状态 |
| 性能状态日志 | 每10分钟 | 输出应用性能状态 |
| 连接池状态日志 | 每5分钟 | 输出连接池状态 |

## 🚀 快速启用

1. **启用健康检查详情**
   ```yaml
   management:
     endpoint:
       health:
         show-details: always
   ```

2. **暴露监控端点**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,prometheus,metrics
   ```

3. **启用启动时间记录**（可选）
   ```java
   public static void main(String[] args) {
       System.setProperty("app.start.time", 
           String.valueOf(System.currentTimeMillis()));
       SpringApplication.run(Application.class, args);
   }
   ```

## 📊 示例输出

### 启动日志
```
✅ 应用启动完成！总耗时: 8523 ms (8.52 s)
════════════════════════════════════
运行环境信息:
  Java 版本: 21.0.1
  CPU 核心数: 12
  内存信息: 最大内存: 4096 MB
════════════════════════════════════
```

### 性能监控日志
```
📊 应用性能状态 - 堆内存: 256MB/1GB (25%), 线程数: 45
💾 数据库连接池状态 - 活跃: 2, 空闲: 8, 使用率: 20%
⚠️ 应用内存使用率较高: 82%, 已使用: 820MB, 最大: 1GB
⚠️ 数据库连接池使用率较高: 85%, 活跃连接: 17/20
```

## 🔗 相关文档

- [性能监控配置集成详细说明](./PERFORMANCE_MONITORING_INTEGRATION.md)
- [Spring Boot Actuator 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer 文档](https://micrometer.io/docs)
- [HikariCP 监控指南](https://github.com/brettwooldridge/HikariCP/wiki/MBean-(JMX)-Monitoring-and-Management)