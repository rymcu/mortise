package com.rymcu.mortise.monitor.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
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

    /**
     * 使用@Lazy注解避免循环依赖
     */
    public ApplicationPerformanceConfig(@Lazy MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // 注册一次性的 Gauge，使用 lambda 提供动态值
        registerPerformanceGauges();
    }

    /**
     * 注册性能监控 Gauge（只注册一次，避免重复注册警告）
     */
    private void registerPerformanceGauges() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        // 注册堆内存使用率 Gauge
        meterRegistry.gauge("application.memory.heap.usage.percent", memoryBean, bean -> {
            var heapMemory = bean.getHeapMemoryUsage();
            long heapMax = heapMemory.getMax();
            return heapMax <= 0 ? 0.0 : (double) heapMemory.getUsed() / heapMax * 100;
        });

        // 注册当前线程数 Gauge
        meterRegistry.gauge("application.threads.current", threadBean, ThreadMXBean::getThreadCount);

        // 注册峰值线程数 Gauge
        meterRegistry.gauge("application.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount);
    }

    /**
     * JVM 性能健康检查指示器
     */
    @Bean
    public HealthIndicator jvmPerformanceHealthIndicator() {
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

            // 健康状态判断
            boolean isHealthy = heapUsage < 0.85 && threadCount < 500;
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
     * 应用性能监控和告警
     * 注意：Gauge 指标已在构造函数中注册，此方法只负责告警检查
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void monitorPerformanceAlerts() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            var heapMemory = memoryBean.getHeapMemoryUsage();
            long heapMax = heapMemory.getMax();
            double heapUsage = heapMax <= 0 ? 0.0 : (double) heapMemory.getUsed() / heapMax;

            // 性能告警
            if (heapUsage > 0.8) {
                log.warn("⚠️ 应用内存使用率较高: {}, 已使用: {}, 最大: {}",
                        String.format("%.1f%%", heapUsage * 100),
                        formatBytes(heapMemory.getUsed()),
                        formatBytes(heapMemory.getMax()));
            }

            if (threadBean.getThreadCount() > 200) {
                log.warn("⚠️ 应用线程数量较多: {} (峰值: {})",
                        threadBean.getThreadCount(),
                        threadBean.getPeakThreadCount());
            }
        } catch (Exception e) {
            log.error("应用性能告警检查失败", e);
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

            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
            long heapMax = heapMemory.getMax();
            double heapUsagePercentVal = heapMax <= 0 ? 0.0 : (double) heapMemory.getUsed() / heapMax * 100;
            String heapUsagePercent = String.format("%.1f", heapUsagePercentVal);

            log.info("📊 应用性能状态 - " +
                            "堆内存: {}/{} ({}%), " +
                            "非堆内存: {}/{}, " +
                            "线程数: {} (峰值: {}, 守护: {})",
                    formatBytes(heapMemory.getUsed()),
                    formatBytes(heapMemory.getMax()),
                    heapUsagePercent,
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
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f%sB", bytes / Math.pow(1024, exp), pre);
    }
}
