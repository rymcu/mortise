package com.rymcu.mortise.config.health;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Resilience4j 限流器健康检查
 *
 * @author ronger
 */
@Slf4j
public record Resilience4jRateLimiterHealthIndicator(
        RateLimiterRegistry rateLimiterRegistry) implements HealthIndicator {

    @Override
    public Health health() {
        try {
            int totalRateLimiters = rateLimiterRegistry.getAllRateLimiters().size();

            Health.Builder builder = Health.up()
                    .withDetail("totalRateLimiters", totalRateLimiters)
                    .withDetail("registryClass", rateLimiterRegistry.getClass().getSimpleName());

            // 检查每个限流器状态
            rateLimiterRegistry.getAllRateLimiters().forEach(rateLimiter -> {
                String name = rateLimiter.getName();
                var metrics = rateLimiter.getMetrics();
                builder.withDetail("rateLimiter." + name + ".availablePermissions", metrics.getAvailablePermissions())
                        .withDetail("rateLimiter." + name + ".waitingThreads", metrics.getNumberOfWaitingThreads());
            });

            return builder.build();

        } catch (Exception e) {
            log.error("Resilience4j 限流器健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorClass", e.getClass().getSimpleName())
                    .build();
        }
    }
}
