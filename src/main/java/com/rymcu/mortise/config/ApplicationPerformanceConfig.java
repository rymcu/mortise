package com.rymcu.mortise.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

/**
 * 应用性能监控配置
 * 提供JVM、系统资源、应用性能等指标监控
 *
 * @author ronger
 */
@Slf4j
@Configuration
@EnableScheduling
public class ApplicationPerformanceConfig {

    private final MeterRegistry meterRegistry;

    // 使用@Lazy注解打破循环依赖
    public ApplicationPerformanceConfig(@Lazy MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 自定义Micrometer注册器，添加通用标签
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "mortise",
                "author", "ronger",
                "domain", "https://rymcu.com"
        );
    }

    /**
     * 应用性能健康检查
     */
    @Bean
    public HealthIndicator applicationPerformanceHealthIndicator() {
        return () -> {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            var heapMemory = memoryBean.getHeapMemoryUsage();
            var nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

            long heapUsed = heapMemory.getUsed();
            long heapMax = heapMemory.getMax();
            double heapUsage = (double) heapUsed / heapMax;

            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();

            // 判断健康状态
            boolean isHealthy = heapUsage < 0.9 && threadCount < 500;

            Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();

            return healthBuilder
                    .withDetail("memory.heap.used", formatBytes(heapUsed))
                    .withDetail("memory.heap.max", formatBytes(heapMax))
                    .withDetail("memory.heap.usage", String.format("%.2f%%", heapUsage * 100))
                    .withDetail("memory.nonHeap.used", formatBytes(nonHeapMemory.getUsed()))
                    .withDetail("memory.nonHeap.max", formatBytes(nonHeapMemory.getMax()))
                    .withDetail("threads.current", threadCount)
                    .withDetail("threads.peak", peakThreadCount)
                    .withDetail("threads.daemon", threadBean.getDaemonThreadCount())
                    .withDetail("performance.status", isHealthy ? "良好" : "需要关注")
                    .build();
        };
    }

    /**
     * 应用性能指标定时收集
     */
    @Scheduled(fixedRate = 60000) // 每分钟收集一次
    public void collectPerformanceMetrics() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            var heapMemory = memoryBean.getHeapMemoryUsage();
            double heapUsage = (double) heapMemory.getUsed() / heapMemory.getMax();

            // 记录自定义指标
            meterRegistry.gauge("application.memory.heap.usage.percent", heapUsage * 100);
            meterRegistry.gauge("application.threads.current", threadBean.getThreadCount());
            meterRegistry.gauge("application.threads.peak", threadBean.getPeakThreadCount());

            // 性能告警
            if (heapUsage > 0.8) {
                log.warn("应用内存使用率较高: {}%, 已使用: {}, 最大: {}",
                        heapUsage * 100,
                        formatBytes(heapMemory.getUsed()),
                        formatBytes(heapMemory.getMax()));
            }

            if (threadBean.getThreadCount() > 200) {
                log.warn("应用线程数量较多: {} (峰值: {})",
                        threadBean.getThreadCount(),
                        threadBean.getPeakThreadCount());
            }

        } catch (Exception e) {
            log.error("收集应用性能指标失败", e);
        }
    }

    /**
     * 应用性能状态定时日志
     */
    @Scheduled(fixedRate = 600000) // 10分钟输出一次
    public void logPerformanceStatus() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            var heapMemory = memoryBean.getHeapMemoryUsage();
            var nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

            double heapUsagePercent = (double) heapMemory.getUsed() / heapMemory.getMax() * 100;

            log.info("应用性能状态 - " +
                    "堆内存: {}/{} ({}%), " +
                    "非堆内存: {}/{}, " +
                    "线程数: {} (峰值: {}, 守护: {})",
                    formatBytes(heapMemory.getUsed()),
                    formatBytes(heapMemory.getMax()),
                    String.format("%.1f", heapUsagePercent),
                    formatBytes(nonHeapMemory.getUsed()),
                    formatBytes(nonHeapMemory.getMax()),
                    threadBean.getThreadCount(),
                    threadBean.getPeakThreadCount(),
                    threadBean.getDaemonThreadCount());

        } catch (Exception e) {
            log.error("输出应用性能状态失败", e);
        }
    }

    /**
     * 格式化字节数显示
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f%s", bytes / Math.pow(1024, exp), pre);
    }
}
