package com.rymcu.mortise.log.health;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志系统健康检查指标
 * 监控日志写入速度、错误日志数量、日志队列状态等
 * 
 * @author ronger
 */
@Slf4j
@Component
public class LogSystemHealthIndicator implements HealthIndicator {

    private final MeterRegistry meterRegistry;
    
    // 日志统计计数器
    private final AtomicLong infoLogs = new AtomicLong(0);
    private final AtomicLong warnLogs = new AtomicLong(0);
    private final AtomicLong errorLogs = new AtomicLong(0);
    private final AtomicLong debugLogs = new AtomicLong(0);
    private final AtomicLong logQueueSize = new AtomicLong(0);
    
    // 最后写入时间
    private volatile LocalDateTime lastLogTime = LocalDateTime.now();

    public LogSystemHealthIndicator(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        try {
            long info = infoLogs.get();
            long warn = warnLogs.get();
            long error = errorLogs.get();
            long debug = debugLogs.get();
            long queueSize = logQueueSize.get();
            long totalLogs = info + warn + error + debug;

            // 计算错误率
            double errorRate = totalLogs > 0 ? (double) error / totalLogs * 100 : 0;
            double warnRate = totalLogs > 0 ? (double) warn / totalLogs * 100 : 0;

            // 记录指标到 Micrometer
            meterRegistry.gauge("log.error.rate", errorRate);
            meterRegistry.gauge("log.warn.rate", warnRate);
            meterRegistry.gauge("log.queue.size", queueSize);
            meterRegistry.gauge("log.total.count", totalLogs);

            // 健康状态判断
            boolean isHealthy = errorRate < 5.0 &&  // 错误率低于5%
                               warnRate < 20.0 &&   // 警告率低于20%
                               queueSize < 10000;   // 队列大小小于10000

            Health.Builder builder = isHealthy ? Health.up() : Health.down();

            // 判断日志活跃度
            long timeSinceLastLog = java.time.Duration.between(lastLogTime, LocalDateTime.now()).toMinutes();
            String logActivity = timeSinceLastLog < 5 ? "活跃" : 
                                timeSinceLastLog < 30 ? "正常" : "静默";

            return builder
                    .withDetail("totalLogs", totalLogs)
                    .withDetail("infoLogs", info)
                    .withDetail("warnLogs", warn)
                    .withDetail("errorLogs", error)
                    .withDetail("debugLogs", debug)
                    .withDetail("errorRate", String.format("%.2f%%", errorRate))
                    .withDetail("warnRate", String.format("%.2f%%", warnRate))
                    .withDetail("queueSize", queueSize)
                    .withDetail("queueStatus", queueSize < 1000 ? "正常" : 
                                              queueSize < 10000 ? "积压" : "严重积压")
                    .withDetail("lastLogTime", lastLogTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .withDetail("logActivity", logActivity)
                    .withDetail("logSystemStatus", isHealthy ? "正常" : "异常")
                    .build();

        } catch (Exception e) {
            log.error("日志系统健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorClass", e.getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * 记录 INFO 级别日志
     */
    public void recordInfoLog() {
        infoLogs.incrementAndGet();
        updateLastLogTime();
    }

    /**
     * 记录 WARN 级别日志
     */
    public void recordWarnLog() {
        warnLogs.incrementAndGet();
        updateLastLogTime();
    }

    /**
     * 记录 ERROR 级别日志
     */
    public void recordErrorLog() {
        errorLogs.incrementAndGet();
        updateLastLogTime();
    }

    /**
     * 记录 DEBUG 级别日志
     */
    public void recordDebugLog() {
        debugLogs.incrementAndGet();
        updateLastLogTime();
    }

    /**
     * 更新日志队列大小
     */
    public void updateLogQueueSize(long size) {
        logQueueSize.set(size);
    }

    /**
     * 更新最后日志时间
     */
    private void updateLastLogTime() {
        lastLogTime = LocalDateTime.now();
    }

    /**
     * 定时重置统计数据
     */
    @Scheduled(fixedRate = 86400000) // 24小时重置一次
    public void resetStatistics() {
        long info = infoLogs.getAndSet(0);
        long warn = warnLogs.getAndSet(0);
        long error = errorLogs.getAndSet(0);
        long debug = debugLogs.getAndSet(0);
        
        log.info("日志系统统计数据已重置 - INFO: {}, WARN: {}, ERROR: {}, DEBUG: {}", 
                info, warn, error, debug);
    }
}