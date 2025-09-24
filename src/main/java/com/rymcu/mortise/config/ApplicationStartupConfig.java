package com.rymcu.mortise.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用启动设置
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
     * 输出运行时环境信息
     */
    private void logRuntimeInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        log.info("运行环境信息:");
        log.info("  Java 版本: {}", System.getProperty("java.version"));
        log.info("  JVM 厂商: {}", System.getProperty("java.vm.vendor"));
        log.info("  JVM 版本: {}", System.getProperty("java.vm.version"));
        log.info("  操作系统: {} {} {}",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        log.info("  CPU 核心数: {}", Runtime.getRuntime().availableProcessors());

        // 输出 JVM 启动参数
        log.info("  JVM 启动参数:");
        java.lang.management.RuntimeMXBean runtimeBean =
                java.lang.management.ManagementFactory.getRuntimeMXBean();
        for (String arg : runtimeBean.getInputArguments()) {
            if (arg.startsWith("-Xm") || arg.startsWith("-XX:") &&
                    (arg.contains("Memory") || arg.contains("Size"))) {
                log.info("    {}", arg);
            }
        }

        log.info("  JVM 内存信息 (堆内存，非系统物理内存):");
        log.info("    最大堆内存(-Xmx): {}MB", maxMemory / 1024 / 1024);
        log.info("    当前分配堆内存: {}MB", totalMemory / 1024 / 1024);
        log.info("    已使用堆内存: {}MB", usedMemory / 1024 / 1024);
        log.info("    堆内可用内存: {}MB", freeMemory / 1024 / 1024);
    }
}
