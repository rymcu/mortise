package com.rymcu.mortise.web.health;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resilience4j RateLimiter 健康检查指示器
 * 监控应用中所有 RateLimiter 的状态和指标
 *
 * @author ronger
 */
@Slf4j
@Component
@ConditionalOnClass(RateLimiterRegistry.class)
public class Resilience4jRateLimiterHealthIndicator implements HealthIndicator {

    private final RateLimiterRegistry rateLimiterRegistry;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public Resilience4jRateLimiterHealthIndicator(Optional<RateLimiterRegistry> registryOptional) {
        this.rateLimiterRegistry = registryOptional.orElse(null);
    }

    @Override
    public Health health() {
        if (rateLimiterRegistry == null) {
            return Health.unknown()
                    .withDetail("reason", "RateLimiterRegistry 未配置")
                    .withDetail("status", "RateLimiter 功能不可用")
                    .build();
        }

        try {
            Map<String, Object> details = new HashMap<>();
            boolean hasUnhealthyLimiters = false;
            int totalLimiters = 0;
            int activeLimiters = 0;

            // 遍历所有已注册的 RateLimiter
            for (RateLimiter rateLimiter : rateLimiterRegistry.getAllRateLimiters()) {
                totalLimiters++;
                String limiterName = rateLimiter.getName();

                // 获取 RateLimiter 的指标
                RateLimiter.Metrics metrics = rateLimiter.getMetrics();
                Map<String, Object> limiterDetails = new HashMap<>();

                // 基本配置信息
                limiterDetails.put("limitForPeriod", metrics.getNumberOfWaitingThreads() >= 0 ? "配置正常" : "配置异常");
                limiterDetails.put("limitRefreshPeriod", rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod().toString());
                limiterDetails.put("timeoutDuration", rateLimiter.getRateLimiterConfig().getTimeoutDuration().toString());

                // 运行时指标
                limiterDetails.put("availablePermissions", metrics.getAvailablePermissions());
                limiterDetails.put("waitingThreads", metrics.getNumberOfWaitingThreads());

                // 判断状态
                boolean isHealthy = true;
                if (metrics.getNumberOfWaitingThreads() > 0) {
                    activeLimiters++;
                    limiterDetails.put("status", "ACTIVE - 有等待线程");
                } else if (metrics.getAvailablePermissions() == 0) {
                    hasUnhealthyLimiters = true;
                    isHealthy = false;
                    limiterDetails.put("status", "EXHAUSTED - 无可用许可");
                } else {
                    limiterDetails.put("status", "HEALTHY - 运行正常");
                }

                details.put("rateLimiter." + limiterName, limiterDetails);
            }

            // 总体统计
            details.put("summary", Map.of(
                "totalRateLimiters", totalLimiters,
                "activeRateLimiters", activeLimiters,
                "registryStatus", "已配置"
            ));

            // 根据整体状态决定健康状态
            Health.Builder healthBuilder;
            if (totalLimiters == 0) {
                healthBuilder = Health.up()
                        .withDetail("message", "RateLimiterRegistry 已配置但未注册任何 RateLimiter");
            } else if (hasUnhealthyLimiters) {
                healthBuilder = Health.down()
                        .withDetail("message", "存在已耗尽的 RateLimiter");
            } else if (activeLimiters > 0) {
                healthBuilder = Health.up()
                        .withDetail("message", "有活跃的限流器正在工作");
            } else {
                healthBuilder = Health.up()
                        .withDetail("message", "所有 RateLimiter 运行正常");
            }

            return healthBuilder.withDetails(details).build();

        } catch (Exception e) {
            log.error("RateLimiter 健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("reason", "检查 RateLimiter 状态时发生异常")
                    .build();
        }
    }
}
