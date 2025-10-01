package com.rymcu.mortise.monitor.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Redis 健康检查指示器
 *
 * @author ronger
 */
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public RedisHealthIndicator(Optional<RedisConnectionFactory> factoryOptional) {
        this.redisConnectionFactory = factoryOptional.orElse(null);
    }

    @Override
    public Health health() {
        if (redisConnectionFactory == null) {
            return Health.unknown()
                    .withDetail("reason", "RedisConnectionFactory 未配置")
                    .build();
        }

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            
            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up()
                        .withDetail("redis", "连接正常")
                        .withDetail("response", pong)
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "响应异常")
                        .withDetail("response", pong)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis 健康检查失败", e);
            return Health.down()
                    .withDetail("redis", "连接失败")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
