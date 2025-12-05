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
import com.rymcu.mortise.cache.constant.CacheConstant;
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
 * 认证模块缓存配置器
 *
 * <p>实现 CacheConfigurer SPI，为认证授权相关的缓存提供配置</p>
 *
 * <p><strong>配置的缓存：</strong></p>
 * <ul>
 *     <li>OAuth2 授权请求缓存（使用专门的序列化器）</li>
 *     <li>JWT Token 缓存</li>
 *     <li>认证令牌缓存</li>
 *     <li>刷新令牌缓存</li>
 *     <li>用户会话缓存</li>
 *     <li>用户在线状态缓存</li>
 *     <li>验证码缓存</li>
 *     <li>密码重置缓存</li>
 *     <li>账号序列缓存</li>
 * </ul>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthCacheConfigurer implements CacheConfigurer {

    @Override
    public int getOrder() {
        return 50; // 认证模块统一优先级
    }

    @Override
    public Map<String, RedisCacheConfiguration> configureCaches(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        // === OAuth2 授权请求缓存（使用专门的序列化器）===
        Jackson2JsonRedisSerializer<Object> oauth2Serializer = createOAuth2JacksonSerializer();
        RedisCacheConfiguration oauth2Config = defaultConfig
                .entryTtl(Duration.ofMinutes(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(oauth2Serializer))
                .disableCachingNullValues();
        configs.put(AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE, oauth2Config);

        // === OAuth2 二维码状态缓存 ===
        configs.put(AuthCacheConstant.OAUTH2_QRCODE_STATE_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.OAUTH2_QRCODE_STATE_EXPIRE_MINUTES)));


        // === OAuth2 参数缓存 ===
        configs.put(AuthCacheConstant.OAUTH2_PARAMETER_MAP_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.OAUTH2_PARAMETER_MAP_EXPIRE_MINUTES)));

        // === JWT Token 缓存 ===
        configs.put(AuthCacheConstant.JWT_TOKEN_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.JWT_TOKEN_EXPIRE_MINUTES)));

        // === 认证令牌缓存 ===
        configs.put(AuthCacheConstant.AUTH_TOKEN_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(CacheConstant.DEFAULT_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.AUTH_REFRESH_TOKEN_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(AuthCacheConstant.REFRESH_TOKEN_EXPIRE_HOURS)));

        // === 用户会话缓存 ===
        configs.put(AuthCacheConstant.USER_SESSION_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(AuthCacheConstant.USER_SESSION_EXPIRE_HOURS)));

        configs.put(AuthCacheConstant.USER_ONLINE_STATUS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.USER_ONLINE_STATUS_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.ACCOUNT_SEQUENCE_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(AuthCacheConstant.ACCOUNT_SEQUENCE_EXPIRE_HOURS)));

        // === 验证码缓存 ===
        configs.put(AuthCacheConstant.LOGIN_VERIFICATION_CODE_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.LOGIN_VERIFICATION_CODE_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.REGISTER_VERIFICATION_CODE_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.REGISTER_VERIFICATION_CODE_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.VERIFICATION_CODE_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.VERIFICATION_CODE_EXPIRE_MINUTES)));

        // === 短信验证码缓存 ===
        configs.put(AuthCacheConstant.SMS_CODE_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.SMS_CODE_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.SMS_CODE_SEND_LIMIT_CACHE,
                defaultConfig.entryTtl(Duration.ofSeconds(AuthCacheConstant.SMS_CODE_SEND_LIMIT_SECONDS)));

        // === 密码重置缓存 ===
        configs.put(AuthCacheConstant.PASSWORD_RESET_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.PASSWORD_RESET_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.PASSWORD_RESET_TOKEN_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(AuthCacheConstant.PASSWORD_RESET_TOKEN_EXPIRE_HOURS)));

        // === 登录限制缓存 ===
        configs.put(AuthCacheConstant.LOGIN_FAIL_COUNT_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(AuthCacheConstant.LOGIN_FAIL_COUNT_EXPIRE_MINUTES)));

        configs.put(AuthCacheConstant.ACCOUNT_LOCK_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(AuthCacheConstant.ACCOUNT_LOCK_EXPIRE_HOURS)));

        log.info("认证模块缓存配置已加载: {} 个缓存策略（包含 OAuth2 授权请求缓存）", configs.size());

        return configs;
    }

    /**
     * 创建专用于 OAuth2 对象的 Jackson 序列化器
     *
     * <p>为 OAuth2AuthorizationRequest 对象提供专门的序列化配置，
     * 支持多态类型处理和 Spring Security 模块</p>
     */
    private Jackson2JsonRedisSerializer<Object> createOAuth2JacksonSerializer() {
        // 创建多态类型验证器，确保安全反序列化
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType(OAuth2AuthorizationRequest.class)
                .build();

        // 使用 JsonMapper.builder 提供更多配置选项
        // 使用 PROPERTY 模式（@class 属性）而不是 WRAPPER_ARRAY 模式
        ObjectMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL,
                        com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY)
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
