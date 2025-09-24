package com.rymcu.mortise.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 增强的缓存配置类
 * 提供多层次、高性能的Redis缓存配置
 *
 * @author ronger
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(RedisTemplate.class)
public class CacheConfig {

    /**
     * 配置 Redis 缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建自定义的Jackson序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 默认缓存配置 - 优化性能和存储
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认 30 分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues() // 不缓存 null 值，避免缓存穿透
                .prefixCacheNameWith("mortise:cache:") // 统一缓存前缀
                .computePrefixWith(cacheName -> "mortise:cache:" + cacheName + ":"); // 自定义键前缀

        // 针对不同业务场景设置不同的缓存策略
        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();

        // === 用户相关缓存 ===
        // 用户信息缓存 - 1 小时，用户信息变化不频繁
        configurationMap.put("userInfo", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 用户会话缓存 - 2 小时，登录状态保持
        configurationMap.put("userSession", defaultConfig.entryTtl(Duration.ofHours(2)));

        // 用户权限缓存 - 30 分钟，权限变化需要及时感知
        configurationMap.put("userPermissions", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // === 权限相关缓存 ===
        // 角色权限缓存 - 2 小时，角色权限相对稳定
        configurationMap.put("rolePermission", defaultConfig.entryTtl(Duration.ofHours(2)));

        // 菜单数据缓存 - 4 小时，菜单结构变化不频繁
        configurationMap.put("menuData", defaultConfig.entryTtl(Duration.ofHours(4)));

        // === 系统配置缓存 ===
        // 字典数据缓存 - 12 小时，字典数据变化很少
        configurationMap.put("dictData", defaultConfig.entryTtl(Duration.ofHours(12)));

        // 系统配置缓存 - 6 小时，系统配置相对稳定
        configurationMap.put("systemConfig", defaultConfig.entryTtl(Duration.ofHours(6)));

        // === 业务数据缓存 ===
        // 热点数据缓存 - 15 分钟，热点数据需要保持较新
        configurationMap.put("hotData", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 统计数据缓存 - 1 小时，统计数据可以有一定延迟
        configurationMap.put("statistics", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 临时数据缓存 - 5 分钟，临时数据短期有效
        configurationMap.put("tempData", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 构建缓存管理器
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        RedisCacheManager cacheManager = RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configurationMap)
                .transactionAware() // 支持事务
                .build();

        log.info("Redis 缓存管理器初始化完成，配置了 {} 个缓存策略", configurationMap.size());

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

        // 优化序列化性能
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

        // 使用构造函数传递 ObjectMapper，避免过时的 setObjectMapper 方法
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
