# Micrometer Gauge 重复注册问题修复

## 问题描述

应用启动后，在日志中出现以下警告：

```
WARN --- [   scheduling-1] i.m.core.instrument.MeterRegistry : 
This Gauge has been already registered 
(MeterId{name='application.memory.heap.usage.percent', tags=[...]}), 
the registration will be ignored. 
Note that subsequent logs will be logged at debug level.
```

## 问题原因

在 `ApplicationPerformanceConfig` 和 `DatabasePerformanceConfig` 中，使用了 `@Scheduled` 定时任务来收集性能指标。这些定时任务每次执行时都调用 `meterRegistry.gauge()` 方法，导致 Micrometer 尝试重复注册相同的 Gauge 指标。

### 错误代码示例

```java
@Scheduled(fixedRate = 60000)
public void collectPerformanceMetrics() {
    // ❌ 每次调用都会尝试注册新的 Gauge
    meterRegistry.gauge("application.memory.heap.usage.percent", heapUsage * 100);
    meterRegistry.gauge("application.threads.current", threadBean.getThreadCount());
}
```

## 解决方案

### 核心思路

Gauge 应该 **只注册一次**，并提供一个动态更新值的函数（lambda 或方法引用）。Micrometer 会在每次抓取指标时自动调用这个函数获取最新值。

### 1. ApplicationPerformanceConfig 修复

**修复前：**
```java
@Scheduled(fixedRate = 60000)
public void collectPerformanceMetrics() {
    meterRegistry.gauge("application.memory.heap.usage.percent", heapUsage * 100);
    meterRegistry.gauge("application.threads.current", threadBean.getThreadCount());
    meterRegistry.gauge("application.threads.peak", threadBean.getPeakThreadCount());
}
```

**修复后：**
```java
public ApplicationPerformanceConfig(@Lazy MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    // 在构造函数中一次性注册所有 Gauge
    registerPerformanceGauges();
}

private void registerPerformanceGauges() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    // ✅ 使用 lambda 提供动态值，只注册一次
    meterRegistry.gauge("application.memory.heap.usage.percent", memoryBean, bean -> {
        var heapMemory = bean.getHeapMemoryUsage();
        long heapMax = heapMemory.getMax();
        return heapMax <= 0 ? 0.0 : (double) heapMemory.getUsed() / heapMax * 100;
    });

    meterRegistry.gauge("application.threads.current", threadBean, ThreadMXBean::getThreadCount);
    meterRegistry.gauge("application.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount);
}

// 定时任务只负责告警检查，不再注册 Gauge
@Scheduled(fixedRate = 60000)
public void monitorPerformanceAlerts() {
    // 只检查性能告警，不再调用 meterRegistry.gauge()
    if (heapUsage > 0.8) {
        log.warn("⚠️ 应用内存使用率较高...");
    }
}
```

### 2. DatabasePerformanceConfig 修复

**修复策略：** 由于 DataSource 可能在构造函数执行时尚未完全初始化，采用 **延迟注册** 策略。

```java
private volatile boolean gaugesRegistered = false;

private void registerConnectionPoolGauges(HikariDataSource hikariDataSource) {
    if (gaugesRegistered) {
        return;
    }
    
    synchronized (this) {
        if (gaugesRegistered) {
            return;
        }
        
        // ✅ 注册 Gauge，使用 lambda 动态获取值
        meterRegistry.gauge("hikari.connections.active", hikariDataSource, ds -> {
            var poolMXBean = ds.getHikariPoolMXBean();
            return poolMXBean != null ? poolMXBean.getActiveConnections() : 0;
        });

        meterRegistry.gauge("hikari.connections.usage", hikariDataSource, ds -> {
            var poolMXBean = ds.getHikariPoolMXBean();
            if (poolMXBean == null) {
                return 0.0;
            }
            int maxSize = ds.getMaximumPoolSize();
            return maxSize > 0 ? (double) poolMXBean.getActiveConnections() / maxSize : 0.0;
        });

        gaugesRegistered = true;
    }
}

@Scheduled(fixedRate = 30000)
public void monitorConnectionPoolAlerts() {
    if (dataSource instanceof HikariDataSource hikariDataSource) {
        // 首次调用时注册 Gauge
        if (!gaugesRegistered) {
            registerConnectionPoolGauges(hikariDataSource);
        }
        
        // 只做告警检查
        // ...
    }
}
```

## Gauge 注册的最佳实践

### ✅ 正确用法

```java
// 方式 1: 使用对象 + lambda
meterRegistry.gauge("metric.name", stateObject, obj -> obj.getValue());

// 方式 2: 使用方法引用
meterRegistry.gauge("thread.count", threadBean, ThreadMXBean::getThreadCount);

// 方式 3: 使用 AtomicInteger/AtomicDouble
AtomicInteger counter = new AtomicInteger(0);
meterRegistry.gauge("counter.value", counter, AtomicInteger::get);

// 方式 4: 使用 Collection 的 size
List<String> items = new ArrayList<>();
meterRegistry.gauge("items.size", items, Collection::size);
```

### ❌ 错误用法

```java
// ❌ 在定时任务中重复调用
@Scheduled(fixedRate = 60000)
public void collectMetrics() {
    meterRegistry.gauge("metric.name", currentValue); // 会导致重复注册警告
}

// ❌ 使用固定值（Gauge 应该是动态的）
meterRegistry.gauge("static.value", 100);
```

## 关键要点

1. **Gauge 只注册一次**：在构造函数或初始化方法中注册，不在定时任务中注册
2. **使用动态值提供者**：通过 lambda 或方法引用让 Micrometer 自动获取最新值
3. **分离职责**：
   - Gauge 注册负责提供指标值
   - 定时任务负责告警检查和日志输出
4. **延迟注册**：对于可能未初始化的对象，使用 `volatile boolean` + `synchronized` 实现线程安全的延迟注册

## 验证方法

1. 重启应用
2. 检查日志，确认不再出现 "This Gauge has been already registered" 警告
3. 访问 `/actuator/prometheus` 或 `/actuator/metrics`，确认指标正常更新
4. 观察 Prometheus/Grafana，确认指标值动态变化

## 涉及文件

- `mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/ApplicationPerformanceConfig.java`
- `mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/DatabasePerformanceConfig.java`

## 参考资料

- [Micrometer Gauge Documentation](https://micrometer.io/docs/concepts#_gauges)
- [Avoiding Duplicate Metric Registration](https://micrometer.io/docs/concepts#_registry)
