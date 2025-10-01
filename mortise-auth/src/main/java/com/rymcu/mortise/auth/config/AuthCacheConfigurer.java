package com.rymcu.mortise.auth.config;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.cache.constant.CacheConstant;
import com.rymcu.mortise.cache.spi.CacheConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证模块缓存配置器
 * 
 * <p>实现 CacheConfigurer SPI，为认证授权相关的缓存提供配置</p>
 * 
 * <p><strong>配置的缓存：</strong></p>
 * <ul>
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
        return 60; // 认证缓存优先级略低于 OAuth2 (50)
    }

    @Override
    public Map<String, RedisCacheConfiguration> configureCaches(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

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

        log.info("认证模块缓存配置已加载: {} 个缓存策略", configs.size());

        return configs;
    }
}
