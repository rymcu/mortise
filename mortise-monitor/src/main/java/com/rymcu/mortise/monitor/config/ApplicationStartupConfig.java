package com.rymcu.mortise.monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用启动配置
 * 记录应用启动信息和运行环境
 * 
 * @author ronger
 */
@Slf4j
@Configuration
public class ApplicationStartupConfig {

    /**
     * 应用就绪监听器
     * 在应用完全启动后记录启动时间和环境信息
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyListener() {
        return event -> {
            String startTimeStr = System.getProperty("app.start.time");
            if (startTimeStr != null) {
                long startTime = Long.parseLong(startTimeStr);
                long readyTime = System.currentTimeMillis();
                long startupTime = readyTime - startTime;
                log.info("✅ 应用启动完成！总耗时: {} ms ({} s)", startupTime, String.format("%.2f", startupTime / 1000.0));
                
                // 输出运行环境信息
                logRuntimeInfo();
            } else {
                log.info("✅ 应用启动完成！");
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

        log.info("════════════════════════════════════════════════════════");
        log.info("运行环境信息:");
        log.info("  Java 版本: {}", System.getProperty("java.version"));
        log.info("  JVM 厂商: {}", System.getProperty("java.vm.vendor"));
        log.info("  JVM 版本: {}", System.getProperty("java.vm.version"));
        log.info("  操作系统: {} {} {}",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        log.info("  CPU 核心数: {}", runtime.availableProcessors());
        log.info("  内存信息:");
        log.info("    最大内存: {} MB", maxMemory / 1024 / 1024);
        log.info("    总内存: {} MB", totalMemory / 1024 / 1024);
        log.info("    已用内存: {} MB", usedMemory / 1024 / 1024);
        log.info("    空闲内存: {} MB", freeMemory / 1024 / 1024);
        log.info("  工作目录: {}", System.getProperty("user.dir"));
        
        String classPath = System.getProperty("java.class.path");
        if (classPath != null && classPath.length() > 100) {
            log.info("  Java 类路径: {}...", classPath.substring(0, 100));
        } else {
            log.info("  Java 类路径: {}", classPath);
        }
        log.info("════════════════════════════════════════════════════════");
    }
}