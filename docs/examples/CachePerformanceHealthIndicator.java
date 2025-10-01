package com.rymcu.mortise.cache.health;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存性能健康检查指标
 * 监控缓存命中率、缓存大小、清理状态等
 * 
 * @author ronger
 */
@Slf4j
@Component
public class CachePerformanceHealthIndicator implements HealthIndicator {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;
    
    // 缓存统计计数器
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);

    public CachePerformanceHealthIndicator(CacheManager cacheManager, 
                                         MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        try {
            // 获取缓存统计信息
            long totalHits = cacheHits.get();
            long totalMisses = cacheMisses.get();
            long totalEvictions = cacheEvictions.get();
            long totalRequests = totalHits + totalMisses;

            // 计算命中率
            double hitRate = totalRequests > 0 ? (double) totalHits / totalRequests * 100 : 0;

            // 统计缓存数量
            int cacheCount = cacheManager.getCacheNames().size();
            
            // 记录指标到 Micrometer
            meterRegistry.gauge("cache.hit.rate", hitRate);
            meterRegistry.gauge("cache.total.requests", totalRequests);
            meterRegistry.gauge("cache.evictions", totalEvictions);
            meterRegistry.gauge("cache.count", cacheCount);

            // 健康状态判断（命中率低于50%视为需要关注）
            boolean isHealthy = hitRate >= 50.0 || totalRequests < 100;
            Health.Builder builder = isHealthy ? Health.up() : Health.down();

            return builder
                    .withDetail("hitRate", String.format("%.2f%%", hitRate))
                    .withDetail("totalHits", totalHits)
                    .withDetail("totalMisses", totalMisses)
                    .withDetail("totalRequests", totalRequests)
                    .withDetail("evictions", totalEvictions)
                    .withDetail("cacheCount", cacheCount)
                    .withDetail("cacheNames", cacheManager.getCacheNames())
                    .withDetail("cacheManagerType", cacheManager.getClass().getSimpleName())
                    .withDetail("performance", isHealthy ? "良好" : "需要优化")
                    .build();

        } catch (Exception e) {
            log.error("缓存性能健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorClass", e.getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    /**
     * 记录缓存清理
     */
    public void recordCacheEviction() {
        cacheEvictions.incrementAndGet();
    }

    /**
     * 定时重置统计数据（避免数据过大）
     */
    @Scheduled(fixedRate = 86400000) // 24小时重置一次
    public void resetStatistics() {
        long hits = cacheHits.getAndSet(0);
        long misses = cacheMisses.getAndSet(0);
        long evictions = cacheEvictions.getAndSet(0);
        
        log.info("缓存统计数据已重置 - 命中: {}, 未命中: {}, 清理: {}", hits, misses, evictions);
    }
}