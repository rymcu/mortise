package com.rymcu.mortise.config;

import com.rymcu.mortise.config.health.Resilience4jRateLimiterHealthIndicator;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j限流配置
 * 简化配置，使用Spring Boot标准的resilience4j配置
 *
 * @author ronger
 */
@Slf4j
@Configuration
public class Resilience4jRateLimitConfig {

    /**
     * 自定义的 RateLimiter Bean（用于兼容现有代码）
     */
    @Bean
    public Resilience4jRateLimiter resilience4jRateLimiter(RateLimiterRegistry rateLimiterRegistry) {
        return new Resilience4jRateLimiter(rateLimiterRegistry);
    }

    /**
     * 注册限流器指标到 Micrometer
     */
    @Bean
    public TaggedRateLimiterMetrics rateLimiterMetrics(RateLimiterRegistry rateLimiterRegistry) {
        return TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry);
    }

    /**
     * 限流器健康检查
     * 使用标准的resilience4j配置中的register-health-indicator替代自定义条件
     */
    @Bean
    public HealthIndicator resilience4jRateLimiterHealthIndicator(RateLimiterRegistry rateLimiterRegistry) {
        return new Resilience4jRateLimiterHealthIndicator(rateLimiterRegistry);
    }
}
