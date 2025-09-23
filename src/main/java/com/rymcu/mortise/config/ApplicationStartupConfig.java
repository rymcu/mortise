package com.rymcu.mortise.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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

        // 输出 JVM 启动参数
        log.info("  JVM启动参数:");
        java.lang.management.RuntimeMXBean runtimeBean =
                java.lang.management.ManagementFactory.getRuntimeMXBean();
        for (String arg : runtimeBean.getInputArguments()) {
            if (arg.startsWith("-Xm") || arg.startsWith("-XX:") &&
                    (arg.contains("Memory") || arg.contains("Size"))) {
                log.info("    {}", arg);
            }
        }

        log.info("  JVM内存信息 (堆内存，非系统物理内存):");
        log.info("    最大堆内存(-Xmx): {}  MB", maxMemory / 1024 / 1024);
        log.info("    当前分配堆内存: {} MB", totalMemory / 1024 / 1024);
        log.info("    已使用堆内存: {} MB", usedMemory / 1024 / 1024);
        log.info("    堆内可用内存: {} MB", freeMemory / 1024 / 1024);
    }
}
