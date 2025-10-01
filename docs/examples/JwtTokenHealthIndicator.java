package com.rymcu.mortise.auth.health;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT Token 健康检查指标
 * 监控活跃 Token 数量、过期 Token 清理状态等
 * 
 * @author ronger
 */
@Slf4j
@Component
public class JwtTokenHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    private final AtomicLong lastCleanupTime = new AtomicLong(System.currentTimeMillis());

    public JwtTokenHealthIndicator(RedisTemplate<String, Object> redisTemplate, 
                                  MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        try {
            // 统计活跃 Token 数量
            Set<String> activeTokens = redisTemplate.keys("mortise:jwtToken:*");
            int activeTokenCount = activeTokens != null ? activeTokens.size() : 0;

            // 统计用户会话数量  
            Set<String> userSessions = redisTemplate.keys("mortise:userSession:*");
            int userSessionCount = userSessions != null ? userSessions.size() : 0;

            // 记录指标到 Micrometer
            meterRegistry.gauge("auth.tokens.active", activeTokenCount);
            meterRegistry.gauge("auth.sessions.active", userSessionCount);

            // 计算上次清理距今的时间
            long timeSinceLastCleanup = System.currentTimeMillis() - lastCleanupTime.get();
            boolean cleanupHealthy = timeSinceLastCleanup < 3600000; // 1小时内有清理

            Health.Builder builder = cleanupHealthy ? Health.up() : Health.down();

            return builder
                    .withDetail("activeTokens", activeTokenCount)
                    .withDetail("activeSessions", userSessionCount)
                    .withDetail("lastCleanupTime", lastCleanupTime.get())
                    .withDetail("timeSinceLastCleanup", timeSinceLastCleanup + "ms")
                    .withDetail("cleanupStatus", cleanupHealthy ? "正常" : "需要清理")
                    .withDetail("redisConnection", "UP")
                    .build();

        } catch (Exception e) {
            log.error("JWT Token 健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorClass", e.getClass().getSimpleName())
                    .withDetail("redisConnection", "DOWN")
                    .build();
        }
    }

    /**
     * 更新最后清理时间（在 Token 清理任务中调用）
     */
    public void updateLastCleanupTime() {
        lastCleanupTime.set(System.currentTimeMillis());
    }
}