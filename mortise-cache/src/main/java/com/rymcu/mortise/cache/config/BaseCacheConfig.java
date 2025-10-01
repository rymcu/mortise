package com.rymcu.mortise.cache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rymcu.mortise.cache.constant.CacheConstant;
import com.rymcu.mortise.cache.spi.CacheConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.*;

/**
 * 基础缓存配置类
 * 提供 Redis 缓存管理器和 RedisTemplate 的基础配置
 * 所有业务缓存配置通过 CacheConfigurer SPI 扩展
 *
 * @author ronger
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(RedisTemplate.class)
public class BaseCacheConfig {

    private final List<CacheConfigurer> cacheConfigurers;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public BaseCacheConfig(Optional<List<CacheConfigurer>> configurersOptional) {
        this.cacheConfigurers = configurersOptional.orElse(null);
    }

    /**
     * 配置 Redis 缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建默认的Jackson序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(CacheConstant.DEFAULT_EXPIRE_MINUTES))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues()
                .prefixCacheNameWith(CacheConstant.CACHE_NAME_PREFIX)
                .computePrefixWith(cacheName -> CacheConstant.CACHE_NAME_PREFIX + cacheName + ":");

        // 收集所有 CacheConfigurer 的配置
        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
        
        if (cacheConfigurers != null && !cacheConfigurers.isEmpty()) {
            log.info("发现 {} 个缓存配置扩展", cacheConfigurers.size());
            
            cacheConfigurers.stream()
                    .sorted(Comparator.comparingInt(CacheConfigurer::getOrder))
                    .forEach(configurer -> {
                        try {
                            Map<String, RedisCacheConfiguration> configs = configurer.configureCaches(defaultConfig);
                            if (configs != null && !configs.isEmpty()) {
                                configurationMap.putAll(configs);
                                log.info("加载缓存配置: {} - {} 个缓存策略", 
                                        configurer.getClass().getSimpleName(), configs.size());
                            }
                        } catch (Exception e) {
                            log.error("加载缓存配置失败: {}", configurer.getClass().getSimpleName(), e);
                        }
                    });
        }

        // 构建缓存管理器
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        RedisCacheManager cacheManager = RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configurationMap)
                .transactionAware()
                .build();

        log.info("Redis 缓存管理器初始化完成，总共配置了 {} 个缓存策略", configurationMap.size());

        return cacheManager;
    }

    /**
     * 创建优化的Jackson序列化器
     */
    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 配置对象映射
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());

        // 优化序列化性能
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    /**
     * 自定义RedisTemplate，用于复杂的缓存操作
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 String 序列化器作为 key 序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 使用 Jackson 序列化器作为 value 序列化器
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = createJacksonSerializer();
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        // 启用事务支持
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();

        log.info("自定义 RedisTemplate 配置完成");

        return template;
    }
}
