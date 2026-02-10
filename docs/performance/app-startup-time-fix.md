# 应用启动时间监控修复

## 🐛 问题描述

`ApplicationStartupConfig` 中使用 `System.getProperty("app.start.time")` 获取启动时间，但是没有在应用启动时设置这个属性，导致启动时间计算功能无法正常工作。

### 问题代码
```java
// ApplicationStartupConfig.java
String startTimeStr = System.getProperty("app.start.time");
if (startTimeStr != null) {
    long startTime = Long.parseLong(startTimeStr);
    long readyTime = System.currentTimeMillis();
    long startupTime = readyTime - startTime;
    log.info("✅ 应用启动完成！总耗时: {} ms ({} s)", startupTime, ...);
}
```

**问题**：`app.start.time` 从未被设置，`startTimeStr` 总是为 `null`，导致总是走 `else` 分支。

## ✅ 解决方案

在 `MortiseApplication.main()` 方法的**最开始**设置启动时间：

### 修复后的代码

```java
@EnableAsync
@SpringBootApplication
public class MortiseApplication {

    public static void main(String[] args) {
        // 记录应用启动时间（用于监控启动耗时）
        System.setProperty("app.start.time", String.valueOf(System.currentTimeMillis()));
        
        SpringApplication.run(MortiseApplication.class, args);
    }
}
```

## 📊 效果

### 修复前
```
✅ 应用启动完成！
════════════════════════════════════════════════════════
运行环境信息:
  Java 版本: 17.0.5
  ...
```

### 修复后
```
✅ 应用启动完成！总耗时: 12345 ms (12.35 s)
════════════════════════════════════════════════════════
运行环境信息:
  Java 版本: 17.0.5
  ...
```

## 🎯 为什么要在 main 方法开始时设置？

1. **最早时机**：`main` 方法是应用的真正入口，在这里设置能捕获最完整的启动时间
2. **包含所有启动阶段**：包括 Spring 上下文初始化、Bean 创建、自动配置等所有耗时
3. **准确性**：从 JVM 启动到应用完全就绪的真实耗时

## 📁 修改文件

| 文件 | 修改内容 |
|------|---------|
| `mortise-app/src/main/java/com/rymcu/mortise/MortiseApplication.java` | 在 `main` 方法开始添加 `System.setProperty("app.start.time", ...)` |

## 🔗 相关配置

### ApplicationStartupConfig.java（无需修改）
```java
@Bean
public ApplicationListener<ApplicationReadyEvent> applicationReadyListener() {
    return event -> {
        String startTimeStr = System.getProperty("app.start.time");
        if (startTimeStr != null) {
            long startTime = Long.parseLong(startTimeStr);
            long readyTime = System.currentTimeMillis();
            long startupTime = readyTime - startTime;
            log.info("✅ 应用启动完成！总耗时: {} ms ({} s)", 
                startupTime, String.format("%.2f", startupTime / 1000.0));
            logRuntimeInfo();
        } else {
            // 如果没有设置启动时间，仍然会输出环境信息
            log.info("✅ 应用启动完成！");
            logRuntimeInfo();
        }
    };
}
```

**说明**：
- ✅ 保持了向后兼容：如果 `app.start.time` 未设置，仍然能正常工作
- ✅ `ApplicationReadyEvent` 在应用完全启动后触发，是计算启动时间的终点
- ✅ 使用 `System.getProperty()` 而非 `@Value`，因为需要在 Spring 上下文初始化前设置

## 🧪 验证

### 1. 编译验证
```bash
mvn clean compile -pl mortise-app -am -q
```

### 2. 运行验证
```bash
mvn spring-boot:run -pl mortise-app
```

### 3. 查看日志
启动后应该能看到：
```
✅ 应用启动完成！总耗时: XXXX ms (XX.XX s)
════════════════════════════════════════════════════════
运行环境信息:
  ...
════════════════════════════════════════════════════════
```

## 💡 扩展：其他启动时间记录方式

### 方式 1：使用 ApplicationStartingEvent（更早）
```java
@Bean
public ApplicationListener<ApplicationStartingEvent> startingListener() {
    return event -> {
        System.setProperty("app.start.time", String.valueOf(System.currentTimeMillis()));
    };
}
```
**缺点**：这个事件监听器本身也是 Spring 管理的，可能不够早。

### 方式 2：使用 SpringApplicationRunListener（最早）
```java
public class StartupTimeRunListener implements SpringApplicationRunListener {
    public StartupTimeRunListener(SpringApplication application, String[] args) {
        System.setProperty("app.start.time", String.valueOf(System.currentTimeMillis()));
    }
}
```
**缺点**：需要在 `META-INF/spring.factories` 中配置，较复杂。

### 方式 3：在 main 方法中设置（✅ 推荐）
```java
public static void main(String[] args) {
    System.setProperty("app.start.time", String.valueOf(System.currentTimeMillis()));
    SpringApplication.run(MortiseApplication.class, args);
}
```
**优点**：
- ✅ 简单直接
- ✅ 时机最早（JVM 启动后第一时间）
- ✅ 无需额外配置
- ✅ 易于理解和维护

**我们选择了方式 3！**

## 📚 相关文档

- [监控架构总结](./monitoring-architecture-summary.md)
- [性能监控快速参考](./performance-monitoring-quick-reference.md)
- [ApplicationStartupConfig 源码](../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/ApplicationStartupConfig.java)

## ✅ 修复清单

- [x] 在 `MortiseApplication.main()` 中设置 `app.start.time`
- [x] 编译验证通过
- [x] 文档已更新

---

**状态**：✅ 已修复  
**影响范围**：启动时间监控功能现在可以正常工作  
**向后兼容**：是（如果未设置 `app.start.time`，仍然会输出环境信息）
