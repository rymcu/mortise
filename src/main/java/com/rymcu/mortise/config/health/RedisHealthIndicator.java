package com.rymcu.mortise.config.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * Redis 健康检查
 *
 * @author ronger
 */
@Component("redisHealthIndicator")
public class RedisHealthIndicator implements HealthIndicator {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Health health() {
        try {
            String pong = stringRedisTemplate.execute(RedisConnectionCommands::ping);

            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("redis", "Available")
                        .withDetail("response", pong)
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "Not responding correctly")
                        .withDetail("response", pong)
                        .build();
            }
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("redis", "Connection failed")
                    .withException(ex)
                    .build();
        }
    }
}
