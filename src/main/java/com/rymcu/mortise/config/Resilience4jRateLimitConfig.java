package com.rymcu.mortise.config;

import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Resilience4j限流配置
 *
 * @author ronger
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(Resilience4jRateLimitConfig.RateLimitProperties.class)
public class Resilience4jRateLimitConfig {

    /**
     * 注册限流器指标到Micrometer
     */
    @Bean
    public TaggedRateLimiterMetrics rateLimiterMetrics(RateLimiterRegistry rateLimiterRegistry) {
        return TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry);
    }

    /**
     * 限流器健康检查
     */
    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.resilience4j.health-check-enabled", havingValue = "true", matchIfMissing = true)
    public HealthIndicator resilience4jRateLimiterHealthIndicator(RateLimiterRegistry rateLimiterRegistry) {
        return new Resilience4jRateLimiterHealthIndicator(rateLimiterRegistry);
    }

    /**
     * 限流配置属性
     */
    @Setter
    @Getter
    @ConfigurationProperties(prefix = "app.rate-limit.resilience4j")
    @Validated
    public static class RateLimitProperties {

        // Getters and Setters
        /**
         * 是否启用Resilience4j限流
         */
        private boolean enabled = true;

        /**
         * 是否启用健康检查
         */
        private boolean healthCheckEnabled = true;

        /**
         * 默认配置
         */
        @NotNull
        private DefaultConfig defaultConfig = new DefaultConfig();

        /**
         * 自定义配置
         */
        private Map<String, CustomConfig> configs;

        /**
         * 默认配置
         */
        @Setter
        @Getter
        public static class DefaultConfig {
            // Getters and Setters
            /**
             * 周期内允许的请求数
             */
            @Min(1)
            private int limitForPeriod = 10;

            /**
             * 刷新周期（秒）
             */
            @Min(1)
            private long refreshPeriodSeconds = 1;

            /**
             * 超时时间（毫秒）
             */
            @Min(0)
            private long timeoutMillis = 100;

        }

        /**
         * 自定义配置
         */
        @Setter
        @Getter
        public static class CustomConfig extends DefaultConfig {
            // Getters and Setters
            /**
             * 配置名称
             */
            private String name;

            /**
             * 配置描述
             */
            private String description;

        }
    }

    /**
         * Resilience4j限流器健康检查
         */
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
                    return Health.down()
                            .withDetail("error", e.getMessage())
                            .withDetail("errorClass", e.getClass().getSimpleName())
                            .build();
                }
            }
        }
}
