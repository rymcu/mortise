# Spring Boot 事件监听器时序分析

## 问题：为什么 ApplicationStartingEvent 在 @Configuration 中无法监听？

### 1. 时序问题

```java
// Spring Boot 启动流程
public static void main(String[] args) {
    // Step 1: 创建 SpringApplication
    SpringApplication app = new SpringApplication(MortiseApplication.class);
    
    // Step 2: 发布 ApplicationStartingEvent ❌ 此时还没有Spring容器
    publishEvent(new ApplicationStartingEvent(app, args));
    
    // Step 3: 创建ApplicationContext容器
    ConfigurableApplicationContext context = createApplicationContext();
    
    // Step 4: 扫描和处理 @Configuration 类 ❌ 太晚了！
    processConfigurationClasses();
    
    // Step 5: 创建 @Bean 监听器 ❌ 太晚了！
    createBeansFromConfiguration();
    
    // Step 6: 发布 ApplicationReadyEvent ✅ 此时容器已就绪
    publishEvent(new ApplicationReadyEvent(app, context, args));
}
```

### 2. 为什么 ApplicationReadyEvent 能在 @Configuration 中工作？

因为 `ApplicationReadyEvent` 在Spring容器**完全初始化后**才发布，这时：
- ✅ 所有 @Configuration 类已处理
- ✅ 所有 @Bean 已创建
- ✅ ApplicationContext 已完全就绪
- ✅ 监听器已注册到容器中

### 3. 为什么 ApplicationStartingEvent 需要在 main 方法中注册？

因为 `ApplicationStartingEvent` 在Spring容器**创建之前**就发布了，这时：
- ❌ Spring容器还不存在
- ❌ @Configuration 类还没被处理
- ❌ @Bean 还没被创建
- ❌ 只能通过编程方式提前注册

## 解决方案对比

### ❌ 错误方式：在 @Configuration 中监听早期事件
```java
@Configuration
public class ApplicationStartupConfig {
    @Bean  // ❌ 太晚了！事件已经错过
    public ApplicationListener<ApplicationStartingEvent> startingListener() {
        return event -> log.info("应用开始启动");
    }
}
```

### ✅ 正确方式：编程式注册早期事件监听器
```java
public class MortiseApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MortiseApplication.class);
        
        // ✅ 在事件发布前注册监听器
        app.addListeners(event -> {
            if (event instanceof ApplicationStartingEvent) {
                System.out.println("🚀 应用开始启动");
            }
        });
        
        app.run(args);
    }
}
```

### ✅ 另一种正确方式：通过 spring.factories
```properties
# src/main/resources/META-INF/spring.factories
org.springframework.context.ApplicationListener=\
com.rymcu.mortise.config.EarlyApplicationListener
```

## 各种事件的监听时机

| 事件类型 | 发布时机 | 能否在@Configuration中监听 | 推荐注册方式 |
|---------|---------|--------------------------|-------------|
| ApplicationStartingEvent | 容器创建前 | ❌ 不能 | 编程式 / spring.factories |
| ApplicationEnvironmentPreparedEvent | 环境准备后，容器创建前 | ❌ 不能 | 编程式 / spring.factories |
| ApplicationContextInitializedEvent | 容器初始化后，配置加载前 | ❌ 不能 | 编程式 / spring.factories |
| ApplicationPreparedEvent | 配置加载后，刷新前 | ⚠️ 部分情况可以 | 编程式 |
| ApplicationStartedEvent | 容器刷新后，CommandLineRunner前 | ✅ 可以 | @Bean |
| ApplicationReadyEvent | 应用完全启动后 | ✅ 可以 | @Bean |
| ApplicationFailedEvent | 启动失败时 | ❌ 不能 | 编程式 / spring.factories |

## 总结

早期事件（如ApplicationStartingEvent）必须在 main 方法中注册，因为：
1. **时序问题**：事件在Spring容器创建前就发布了
2. **容器生命周期**：@Configuration 和 @Bean 在容器创建后才处理
3. **监听器注册**：必须在事件发布前注册才能监听到

这就是为什么需要在 `MortiseApplication` 中添加监听器的根本原因！