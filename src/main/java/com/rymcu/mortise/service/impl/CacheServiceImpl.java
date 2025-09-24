package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.core.constant.CacheConstant;
import com.rymcu.mortise.core.constant.ProjectConstant;
import com.rymcu.mortise.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 统一缓存服务实现类
 * 使用 Spring Cache 抽象统一管理所有业务缓存
 *
 * @author ronger
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    @Resource
    private CacheManager cacheManager;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";

    @Override
    public void storeVerificationCode(String email, String code) {
        Cache cache = cacheManager.getCache(CacheConstant.VERIFICATION_CODE_CACHE);
        if (cache != null) {
            String key = ProjectConstant.REDIS_REGISTER + email;
            cache.put(key, code);
            log.debug("存储验证码：{} -> {}", key, code);
        }
    }

    @Override
    public String getVerificationCode(String email) {
        Cache cache = cacheManager.getCache(CacheConstant.VERIFICATION_CODE_CACHE);
        if (cache != null) {
            String key = ProjectConstant.REDIS_REGISTER + email;
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                String code = (String) wrapper.get();
                log.debug("获取验证码：{} -> {}", key, code);
                return code;
            }
        }
        log.debug("验证码不存在或已过期：{}", ProjectConstant.REDIS_REGISTER + email);
        return null;
    }

    @Override
    public void removeVerificationCode(String email) {
        Cache cache = cacheManager.getCache(CacheConstant.VERIFICATION_CODE_CACHE);
        if (cache != null) {
            String key = ProjectConstant.REDIS_REGISTER + email;
            cache.evict(key);
            log.debug("删除验证码：{}", key);
        }
    }

    @Override
    public void storeRefreshToken(String refreshToken, String account) {
        Cache cache = cacheManager.getCache(CacheConstant.AUTH_REFRESH_TOKEN_CACHE);
        if (cache != null) {
            String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
            cache.put(key, account);
            log.debug("存储刷新令牌：{} -> {}", key, account);
        }
    }

    @Override
    public String getAccountByRefreshToken(String refreshToken) {
        Cache cache = cacheManager.getCache(CacheConstant.AUTH_REFRESH_TOKEN_CACHE);
        if (cache != null) {
            String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                String account = (String) wrapper.get();
                log.debug("获取刷新令牌对应账号：{} -> {}", key, account);
                return account;
            }
        }
        log.debug("刷新令牌不存在或已过期：{}", REFRESH_TOKEN_KEY_PREFIX + refreshToken);
        return null;
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        Cache cache = cacheManager.getCache(CacheConstant.AUTH_REFRESH_TOKEN_CACHE);
        if (cache != null) {
            String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
            cache.evict(key);
            log.debug("删除刷新令牌：{}", key);
        }
    }

    @Override
    public void storePasswordResetToken(String resetToken, String email) {
        Cache cache = cacheManager.getCache(CacheConstant.PASSWORD_RESET_CACHE);
        if (cache != null) {
            cache.put(resetToken, email);
            log.debug("存储密码重置令牌：{} -> {}", resetToken, email);
        }
    }

    @Override
    public String getEmailByResetToken(String resetToken) {
        Cache cache = cacheManager.getCache(CacheConstant.PASSWORD_RESET_CACHE);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(resetToken);
            if (wrapper != null) {
                String email = (String) wrapper.get();
                log.debug("获取密码重置令牌对应邮箱：{} -> {}", resetToken, email);
                return email;
            }
        }
        log.debug("密码重置令牌不存在或已过期：{}", resetToken);
        return null;
    }

    @Override
    public void removePasswordResetToken(String resetToken) {
        Cache cache = cacheManager.getCache(CacheConstant.PASSWORD_RESET_CACHE);
        if (cache != null) {
            cache.evict(resetToken);
            log.debug("删除密码重置令牌：{}", resetToken);
        }
    }

    // ========== 通用缓存操作实现 ==========

    @Override
    public void putString(String cacheName, String key, String value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("存储字符串到缓存：{} -> {} = {}", cacheName, key, value);
        }
    }

    @Override
    public String getString(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                String value = (String) wrapper.get();
                log.debug("从缓存获取字符串：{} -> {} = {}", cacheName, key, value);
                return value;
            }
        }
        log.debug("缓存中不存在：{} -> {}", cacheName, key);
        return null;
    }

    @Override
    public void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("删除缓存：{} -> {}", cacheName, key);
        }
    }

    @Override
    public void putObject(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("存储对象到缓存：{} -> {} = {}", cacheName, key, value.getClass().getSimpleName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(String cacheName, String key, Class<T> clazz) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                Object value = wrapper.get();
                if (clazz.isInstance(value)) {
                    log.debug("从缓存获取对象：{} -> {} = {}", cacheName, key, clazz.getSimpleName());
                    return (T) value;
                }
                log.warn("缓存对象类型不匹配：{} -> {}，期望：{}，实际：{}",
                        cacheName, key, clazz.getSimpleName(), value.getClass().getSimpleName());
            }
        }
        log.debug("缓存中不存在：{} -> {}", cacheName, key);
        return null;
    }

    // ========== 用户相关缓存操作实现 ==========

    private static final String CURRENT_ACCOUNT_KEY = "current:account";

    @Override
    public void storeCurrentAccount(String currentAccount) {
        putString(CacheConstant.ACCOUNT_SEQUENCE_CACHE, CURRENT_ACCOUNT_KEY, currentAccount);
    }

    @Override
    public String getCurrentAccount() {
        return getString(CacheConstant.ACCOUNT_SEQUENCE_CACHE, CURRENT_ACCOUNT_KEY);
    }

    @Override
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        String userIdStr = userId.toString();

        // 清除用户信息缓存
        evict(CacheConstant.USER_INFO_CACHE, userIdStr);

        // 清除用户会话缓存
        evict(CacheConstant.USER_SESSION_CACHE, userIdStr);

        // 清除用户权限缓存
        evict(CacheConstant.USER_PERMISSIONS_CACHE, userIdStr);

        log.info("清除用户相关缓存完成，用户 ID：{}", userId);
    }

    @Override
    public void evictAllUserCache() {
        String[] userCacheNames = {
            CacheConstant.USER_INFO_CACHE,
            CacheConstant.USER_SESSION_CACHE,
            CacheConstant.USER_PERMISSIONS_CACHE
        };

        for (String cacheName : userCacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("清除缓存：{}", cacheName);
            }
        }

        log.info("清除所有用户缓存完成");
    }

    // ========== JWT Token 相关缓存操作实现 ==========

    @Override
    public void storeJwtToken(String account, String token) {
        putString(CacheConstant.JWT_TOKEN_CACHE, account, token);
        log.debug("存储 JWT Token：{} -> {}", account, token);
    }

    @Override
    public String getJwtToken(String account) {
        String token = getString(CacheConstant.JWT_TOKEN_CACHE, account);
        log.debug("获取 JWT Token：{} -> {}", account, token != null ? "存在" : "不存在");
        return token;
    }

    @Override
    public void removeJwtToken(String account) {
        evict(CacheConstant.JWT_TOKEN_CACHE, account);
        log.debug("删除 JWT Token：{}", account);
    }

    @Override
    public void storeUserOnlineStatus(String account, String lastOnlineTime) {
        String key = "last_online:" + account;
        putString(CacheConstant.USER_ONLINE_STATUS_CACHE, key, lastOnlineTime);
        log.debug("存储用户在线状态：{} -> {}", account, lastOnlineTime);
    }

    @Override
    public String getUserOnlineStatus(String account) {
        String key = "last_online:" + account;
        String lastOnlineTime = getString(CacheConstant.USER_ONLINE_STATUS_CACHE, key);
        log.debug("获取用户在线状态：{} -> {}", account, lastOnlineTime != null ? lastOnlineTime : "不存在");
        return lastOnlineTime;
    }

    // ========== OAuth2 相关缓存操作实现 ==========

    @Override
    public void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest) {
        putObject(CacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE, state, authorizationRequest);
        log.debug("存储 OAuth2 授权请求：{}", state);
    }

    @Override
    public <T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz) {
        T request = getObject(CacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE, state, clazz);
        log.debug("获取 OAuth2 授权请求：{} -> {}", state, request != null ? "存在" : "不存在");
        return request;
    }

    @Override
    public void removeOAuth2AuthorizationRequest(String state) {
        evict(CacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE, state);
        log.debug("删除 OAuth2 授权请求：{}", state);
    }

    // ========== 字典缓存操作实现 ==========

    @Override
    public void storeDictOptions(String dictTypeCode, Object options) {
        putObject(CacheConstant.DICT_DATA_CACHE, dictTypeCode, options);
        log.debug("存储字典选项：{}", dictTypeCode);
    }

    @Override
    public <T> T getDictOptions(String dictTypeCode, Class<T> clazz) {
        T options = getObject(CacheConstant.DICT_DATA_CACHE, dictTypeCode, clazz);
        log.debug("获取字典选项：{} -> {}", dictTypeCode, options != null ? "命中缓存" : "缓存未命中");
        return options;
    }

    @Override
    public void removeDictOptions(String dictTypeCode) {
        evict(CacheConstant.DICT_DATA_CACHE, dictTypeCode);
        log.debug("删除字典选项缓存：{}", dictTypeCode);
    }

    @Override
    public void removeAllDictOptions() {
        Cache cache = cacheManager.getCache(CacheConstant.DICT_DATA_CACHE);
        if (cache != null) {
            cache.clear();
            log.debug("清除所有字典选项缓存");
        }
    }

    @Override
    public void removeDictOptionsBatch(java.util.List<String> dictTypeCodes) {
        if (dictTypeCodes != null && !dictTypeCodes.isEmpty()) {
            Cache cache = cacheManager.getCache(CacheConstant.DICT_DATA_CACHE);
            if (cache != null) {
                for (String dictTypeCode : dictTypeCodes) {
                    cache.evict(dictTypeCode);
                }
                log.debug("批量删除字典选项缓存：{}", dictTypeCodes);
            }
        }
    }
}
