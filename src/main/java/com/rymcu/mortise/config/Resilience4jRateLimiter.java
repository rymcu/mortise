package com.rymcu.mortise.config;

import com.rymcu.mortise.core.exception.RateLimitException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 基于Resilience4j的限流器
 * 提供更丰富的限流功能和监控支持
 *
 * @author ronger
 */
@Slf4j
@Component
public class Resilience4jRateLimiter {

    private final RateLimiterRegistry rateLimiterRegistry;

    public Resilience4jRateLimiter() {
        // 默认配置
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1)) // 刷新周期
                .limitForPeriod(10) // 每个周期允许的请求数
                .timeoutDuration(Duration.ofMillis(100)) // 超时时间
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(defaultConfig);
    }

    /**
     * 限流检查
     *
     * @param key            限流key
     * @param limitForPeriod 周期内允许的请求数
     * @param refreshPeriod  刷新周期（秒）
     * @param timeout        超时时间（毫秒）
     * @param errorMessage   错误消息
     * @throws RateLimitException 超过限制时抛出异常
     */
    public void checkRateLimit(String key, int limitForPeriod, long refreshPeriod, long timeout, String errorMessage) {
        RateLimiter rateLimiter = getRateLimiter(key, limitForPeriod, refreshPeriod, timeout);

        // 尝试获取许可
        boolean permitted = rateLimiter.acquirePermission();
        if (!permitted) {
            log.warn("Resilience4j限流触发: key={}, limitForPeriod={}, refreshPeriod={}s",
                    key, limitForPeriod, refreshPeriod);
            throw new RateLimitException(errorMessage);
        }

        log.debug("Resilience4j限流检查通过: key={}", key);
    }

    /**
     * 装饰器模式执行限流
     *
     * @param key            限流key
     * @param limitForPeriod 周期内允许的请求数
     * @param refreshPeriod  刷新周期（秒）
     * @param timeout        超时时间（毫秒）
     * @param supplier       要执行的操作
     * @param errorMessage   错误消息
     * @return 执行结果
     */
    public <T> T executeWithRateLimit(String key, int limitForPeriod, long refreshPeriod,
                                     long timeout, Supplier<T> supplier, String errorMessage) {
        RateLimiter rateLimiter = getRateLimiter(key, limitForPeriod, refreshPeriod, timeout);

        // 使用装饰器模式
        Supplier<T> decoratedSupplier = RateLimiter.decorateSupplier(rateLimiter, supplier);

        try {
            return decoratedSupplier.get();
        } catch (io.github.resilience4j.ratelimiter.RequestNotPermitted e) {
            log.warn("Resilience4j限流触发: key={}, limitForPeriod={}, refreshPeriod={}s",
                    key, limitForPeriod, refreshPeriod);
            throw new RateLimitException(errorMessage);
        }
    }

    /**
     * 获取或创建限流器
     */
    private RateLimiter getRateLimiter(String key, int limitForPeriod, long refreshPeriod, long timeout) {
        return rateLimiterRegistry.rateLimiter(key, RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(refreshPeriod))
                .limitForPeriod(limitForPeriod)
                .timeoutDuration(Duration.ofMillis(timeout))
                .build());
    }

    /**
     * 获取限流器状态
     */
    public RateLimiterStatus getRateLimiterStatus(String key) {
        Optional<RateLimiter> rateLimiterOpt = rateLimiterRegistry.find(key);
        if (rateLimiterOpt.isEmpty()) {
            return null;
        }

        RateLimiter rateLimiter = rateLimiterOpt.get();
        RateLimiter.Metrics metrics = rateLimiter.getMetrics();

        return RateLimiterStatus.builder()
                .name(key)
                .availablePermissions(metrics.getAvailablePermissions())
                .numberOfWaitingThreads(metrics.getNumberOfWaitingThreads())
                .build();
    }

    /**
     * 移除限流器
     */
    public void removeRateLimiter(String key) {
        rateLimiterRegistry.remove(key);
        log.debug("移除Resilience4j限流器: key={}", key);
    }

    /**
     * 限流器状态
     */
    @Getter
    public static class RateLimiterStatus {
        // Getters
        private String name;
        private int availablePermissions;
        private int numberOfWaitingThreads;

        public static RateLimiterStatusBuilder builder() {
            return new RateLimiterStatusBuilder();
        }

        public static class RateLimiterStatusBuilder {
            private String name;
            private int availablePermissions;
            private int numberOfWaitingThreads;

            public RateLimiterStatusBuilder name(String name) {
                this.name = name;
                return this;
            }

            public RateLimiterStatusBuilder availablePermissions(int availablePermissions) {
                this.availablePermissions = availablePermissions;
                return this;
            }

            public RateLimiterStatusBuilder numberOfWaitingThreads(int numberOfWaitingThreads) {
                this.numberOfWaitingThreads = numberOfWaitingThreads;
                return this;
            }

            public RateLimiterStatus build() {
                RateLimiterStatus status = new RateLimiterStatus();
                status.name = this.name;
                status.availablePermissions = this.availablePermissions;
                status.numberOfWaitingThreads = this.numberOfWaitingThreads;
                return status;
            }
        }
    }
}
