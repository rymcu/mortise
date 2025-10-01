package com.rymcu.mortise.auth.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.cache.spi.CacheConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 缓存配置器
 * 实现 CacheConfigurer SPI，为 OAuth2 授权请求提供专门的序列化配置
 * 
 * 对应原 CacheConfig 中的 OAuth2 授权请求缓存配置
 *
 * @author ronger
 */
@Slf4j
@Component
public class OAuth2CacheConfigurer implements CacheConfigurer {

    @Override
    public int getOrder() {
        return 50; // 较高优先级
    }

    @Override
    public Map<String, RedisCacheConfiguration> configureCaches(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        // 创建专门用于 OAuth2 的 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> oauth2Serializer = createOAuth2JacksonSerializer();

        // OAuth2 授权请求缓存 - 10分钟，使用专门的序列化器
        RedisCacheConfiguration oauth2Config = defaultConfig
                .entryTtl(Duration.ofMinutes(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(oauth2Serializer))
                .disableCachingNullValues();

        configs.put(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE, oauth2Config);

        log.info("OAuth2 缓存配置已加载: 1 个缓存策略");

        return configs;
    }

    /**
     * 创建专用于 OAuth2 对象的 Jackson 序列化器
     * 对应原 CacheConfig.createOAuth2JacksonSerializer 方法
     */
    private Jackson2JsonRedisSerializer<Object> createOAuth2JacksonSerializer() {
        // 创建多态类型验证器，确保安全反序列化
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType(OAuth2AuthorizationRequest.class)
                .build();

        // 使用 JsonMapper.builder 提供更多配置选项
        ObjectMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                .build();

        // 注册 Java 8 日期时间模块
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 注册 Spring Security 的 Jackson 模块
        ClassLoader loader = getClass().getClassLoader();
        List<com.fasterxml.jackson.databind.Module> modules = SecurityJackson2Modules.getModules(loader);
        mapper.registerModules(modules);

        // 配置其他属性
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
    }
}
