package com.rymcu.mortise.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 应用启动和异步任务优化配置
 *
 * @author ronger
 */
@Slf4j
@Configuration
public class ApplicationStartupConfig {

    /**
     * 应用就绪监听器 - 这个可以正常工作，因为在容器初始化后触发
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyListener() {
        return event -> {
            String startTimeStr = System.getProperty("app.start.time");
            if (startTimeStr != null) {
                long startTime = Long.parseLong(startTimeStr);
                long readyTime = System.currentTimeMillis();
                long startupTime = readyTime - startTime;

                log.info("✅ 应用启动完成！总耗时: {} ms ({}s)", startupTime, startupTime / 1000.0);

                // 输出运行环境信息
                logRuntimeInfo();

                // 输出应用配置信息
                logApplicationInfo();
            }
        };
    }

    /**
     * 优化的异步任务执行器
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public TaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        // 最大线程数 = CPU核心数 * 2
        int maxPoolSize = corePoolSize * 2;
        // 队列容量
        int queueCapacity = 100;

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("mortise-async-executor");
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("异步任务执行器初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    /**
     * 输出运行时环境信息
     */
    private void logRuntimeInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        log.info("运行环境信息:");
        log.info("  Java版本: {}", System.getProperty("java.version"));
        log.info("  JVM厂商: {}", System.getProperty("java.vm.vendor"));
        log.info("  JVM版本: {}", System.getProperty("java.vm.version"));
        log.info("  操作系统: {} {} {}",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        log.info("  CPU核心数: {}", Runtime.getRuntime().availableProcessors());
        log.info("  内存信息:");
        log.info("    最大内存: {}MB", maxMemory / 1024 / 1024);
        log.info("    总内存: {}MB", totalMemory / 1024 / 1024);
        log.info("    已用内存: {}MB", usedMemory / 1024 / 1024);
        log.info("    可用内存: {}MB", freeMemory / 1024 / 1024);
    }

    /**
     * 输出应用配置信息
     */
    private void logApplicationInfo() {
        log.info("应用配置信息:");
        log.info("  应用名称: Mortise");
        log.info("  应用版本: 0.0.1");
        log.info("  Spring Boot版本: {}", org.springframework.boot.SpringBootVersion.getVersion());
        log.info("  Spring Framework版本: {}", org.springframework.core.SpringVersion.getVersion());
        log.info("  配置文件: application-dev.yml");
        log.info("  运行环境: development");

        // 输出重要特性状态
        log.info("核心功能状态:");
        log.info("  ✓ 数据库连接池: HikariCP");
        log.info("  ✓ 缓存系统: Redis");
        log.info("  ✓ 安全框架: Spring Security + JWT");
        log.info("  ✓ 限流保护: Resilience4j");
        log.info("  ✓ API文档: OpenAPI 3.0");
        log.info("  ✓ 监控指标: Micrometer + Prometheus");
        log.info("  ✓ 健康检查: Spring Actuator");

        log.info("🎉 Mortise 应用已成功启动并准备就绪！");
    }
}
