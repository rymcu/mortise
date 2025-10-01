package com.rymcu.mortise.cache.config;

import com.rymcu.mortise.cache.listener.RedisKeyExpirationListener;
import com.rymcu.mortise.cache.spi.CacheExpirationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;
import java.util.Optional;

/**
 * 缓存模块自动配置类
 * 自动配置 Redis 监听器和相关组件
 * 
 * @author ronger
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(RedisProperties.class)
public class CacheAutoConfiguration {

    /**
     * Redis 消息监听器容器
     * 用于监听 Redis 键过期事件
     */
    @Bean
    @ConditionalOnProperty(name = "mortise.cache.redis.listener.enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        log.info("Redis 消息监听器容器已配置");
        return container;
    }

    /**
     * Redis 键过期事件监听器
     * 基于 SPI 机制处理缓存失效事件
     */
    @Bean
    @ConditionalOnProperty(name = "mortise.cache.redis.expiration-listener.enabled", havingValue = "true", matchIfMissing = true)
    public RedisKeyExpirationListener redisKeyExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            Optional<List<CacheExpirationHandler>> handlersOptional) {
        return new RedisKeyExpirationListener(listenerContainer, handlersOptional);
    }
}