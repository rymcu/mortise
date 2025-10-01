package com.rymcu.mortise.notification.health;

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
 * 通知服务健康检查指标
 * 监控消息发送成功率、队列积压、发送延迟等
 * 
 * @author ronger
 */
@Slf4j
@Component
public class NotificationHealthIndicator implements HealthIndicator {

    private final MeterRegistry meterRegistry;
    
    // 消息统计计数器
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    private final AtomicLong messagesQueued = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    
    // 最后处理时间
    private volatile LocalDateTime lastProcessedTime = LocalDateTime.now();

    public NotificationHealthIndicator(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        try {
            long sent = messagesSent.get();
            long failed = messagesFailed.get();
            long queued = messagesQueued.get();
            long totalTime = totalProcessingTime.get();
            long totalMessages = sent + failed;

            // 计算成功率
            double successRate = totalMessages > 0 ? (double) sent / totalMessages * 100 : 100;
            
            // 计算平均处理时间
            double avgProcessingTime = sent > 0 ? (double) totalTime / sent : 0;

            // 记录指标到 Micrometer
            meterRegistry.gauge("notification.success.rate", successRate);
            meterRegistry.gauge("notification.queue.size", queued);
            meterRegistry.gauge("notification.avg.processing.time", avgProcessingTime);
            meterRegistry.gauge("notification.total.sent", sent);
            meterRegistry.gauge("notification.total.failed", failed);

            // 健康状态判断
            boolean isHealthy = successRate >= 95.0 && queued < 1000 && avgProcessingTime < 5000;
            Health.Builder builder = isHealthy ? Health.up() : Health.down();

            // 检查是否有消息积压
            String queueStatus = queued < 100 ? "正常" : 
                                queued < 1000 ? "轻微积压" : "严重积压";
            
            return builder
                    .withDetail("successRate", String.format("%.2f%%", successRate))
                    .withDetail("messagesSent", sent)
                    .withDetail("messagesFailed", failed)
                    .withDetail("messagesQueued", queued)
                    .withDetail("queueStatus", queueStatus)
                    .withDetail("avgProcessingTime", String.format("%.2f ms", avgProcessingTime))
                    .withDetail("lastProcessedTime", lastProcessedTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .withDetail("performance", isHealthy ? "良好" : "需要关注")
                    .build();

        } catch (Exception e) {
            log.error("通知服务健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorClass", e.getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * 记录消息发送成功
     */
    public void recordMessageSent(long processingTimeMs) {
        messagesSent.incrementAndGet();
        totalProcessingTime.addAndGet(processingTimeMs);
        lastProcessedTime = LocalDateTime.now();
        
        // 如果是排队的消息，减少队列计数
        long currentQueued = messagesQueued.get();
        if (currentQueued > 0) {
            messagesQueued.decrementAndGet();
        }
    }

    /**
     * 记录消息发送失败
     */
    public void recordMessageFailed() {
        messagesFailed.incrementAndGet();
        lastProcessedTime = LocalDateTime.now();
        
        // 如果是排队的消息，减少队列计数
        long currentQueued = messagesQueued.get();
        if (currentQueued > 0) {
            messagesQueued.decrementAndGet();
        }
    }

    /**
     * 记录消息入队
     */
    public void recordMessageQueued() {
        messagesQueued.incrementAndGet();
    }

    /**
     * 定时重置统计数据
     */
    @Scheduled(fixedRate = 86400000) // 24小时重置一次
    public void resetStatistics() {
        long sent = messagesSent.getAndSet(0);
        long failed = messagesFailed.getAndSet(0);
        totalProcessingTime.set(0);
        
        log.info("通知服务统计数据已重置 - 发送成功: {}, 发送失败: {}, 当前队列: {}", 
                sent, failed, messagesQueued.get());
    }
}