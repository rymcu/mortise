package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.system.service.SystemCacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * 系统缓存服务实现
 * 封装基础设施层的 CacheService，提供业务语义化操作
 *
 * @author ronger
 */
@Slf4j
@Service
public class SystemCacheServiceImpl implements SystemCacheService {

    @Resource
    private CacheService cacheService;

    private static final String USER_INFO_KEY_PREFIX = "user:info:";
    private static final String USER_PERMISSIONS_KEY_PREFIX = "user:permissions:";
    private static final String USER_MENUS_KEY_PREFIX = "user:menus:";
    private static final String DICT_DATA_KEY_PREFIX = "dict:data:";
    private static final String SYSTEM_CONFIG_KEY_PREFIX = "system:config:";
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification:code:";

    // ==================== 用户相关缓存 ====================

    @Override
    public void cacheUserInfo(Long userId, Object userInfo) {
        String key = USER_INFO_KEY_PREFIX + userId;
        cacheService.set(key, userInfo, Duration.ofHours(SystemCacheConstant.USER_INFO_EXPIRE_HOURS));
        log.debug("缓存用户信息: userId={}", userId);
    }

    @Override
    public <T> T getUserInfo(Long userId, Class<T> type) {
        String key = USER_INFO_KEY_PREFIX + userId;
        return cacheService.get(key, type);
    }

    @Override
    public void evictUserInfo(Long userId) {
        String key = USER_INFO_KEY_PREFIX + userId;
        cacheService.delete(key);
        log.debug("清除用户信息缓存: userId={}", userId);
    }

    @Override
    public void cacheUserPermissions(Long userId, Set<String> permissions) {
        String key = USER_PERMISSIONS_KEY_PREFIX + userId;
        cacheService.set(key, permissions, Duration.ofMinutes(SystemCacheConstant.USER_PERMISSIONS_EXPIRE_MINUTES));
        log.debug("缓存用户权限: userId={}, permissions={}", userId, permissions.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(Long userId) {
        String key = USER_PERMISSIONS_KEY_PREFIX + userId;
        return cacheService.get(key, Set.class);
    }

    @Override
    public void evictUserPermissions(Long userId) {
        String key = USER_PERMISSIONS_KEY_PREFIX + userId;
        cacheService.delete(key);
        log.debug("清除用户权限缓存: userId={}", userId);
    }

    // ==================== 字典相关缓存 ====================

    @Override
    public void cacheDictData(String dictType, Object dictData) {
        String key = DICT_DATA_KEY_PREFIX + dictType;
        cacheService.set(key, dictData, Duration.ofHours(SystemCacheConstant.DICT_DATA_EXPIRE_HOURS));
        log.debug("缓存字典数据: dictType={}", dictType);
    }

    @Override
    public <T> T getDictData(String dictType, Class<T> type) {
        String key = DICT_DATA_KEY_PREFIX + dictType;
        return cacheService.get(key, type);
    }

    @Override
    public void evictDictData(String dictType) {
        String key = DICT_DATA_KEY_PREFIX + dictType;
        cacheService.delete(key);
        log.debug("清除字典数据缓存: dictType={}", dictType);
    }

    @Override
    public void evictAllDictData() {
        String pattern = DICT_DATA_KEY_PREFIX + "*";
        cacheService.deletePattern(pattern);
        log.info("清除所有字典数据缓存");
    }

    // ==================== 菜单相关缓存 ====================

    @Override
    public void cacheUserMenus(Long userId, Object menus) {
        String key = USER_MENUS_KEY_PREFIX + userId;
        cacheService.set(key, menus, Duration.ofHours(SystemCacheConstant.MENU_DATA_EXPIRE_HOURS));
        log.debug("缓存用户菜单: userId={}", userId);
    }

    @Override
    public <T> T getUserMenus(Long userId, Class<T> type) {
        String key = USER_MENUS_KEY_PREFIX + userId;
        return cacheService.get(key, type);
    }

    @Override
    public void evictUserMenus(Long userId) {
        String key = USER_MENUS_KEY_PREFIX + userId;
        cacheService.delete(key);
        log.debug("清除用户菜单缓存: userId={}", userId);
    }

    // ==================== 配置相关缓存 ====================

    @Override
    public void cacheSystemConfig(String configKey, Object configValue) {
        String key = SYSTEM_CONFIG_KEY_PREFIX + configKey;
        cacheService.set(key, configValue, Duration.ofHours(SystemCacheConstant.SYSTEM_CONFIG_EXPIRE_HOURS));
        log.debug("缓存系统配置: configKey={}", configKey);
    }

    @Override
    public <T> T getSystemConfig(String configKey, Class<T> type) {
        String key = SYSTEM_CONFIG_KEY_PREFIX + configKey;
        return cacheService.get(key, type);
    }

    @Override
    public void evictSystemConfig(String configKey) {
        String key = SYSTEM_CONFIG_KEY_PREFIX + configKey;
        cacheService.delete(key);
        log.debug("清除系统配置缓存: configKey={}", configKey);
    }

    // ==================== 验证码相关缓存 ====================

    @Override
    public void cacheVerificationCode(String key, String code, Duration timeout) {
        String cacheKey = VERIFICATION_CODE_KEY_PREFIX + key;
        cacheService.set(cacheKey, code, timeout);
        log.debug("缓存验证码: key={}, timeout={}", key, timeout);
    }

    @Override
    public String getVerificationCode(String key) {
        String cacheKey = VERIFICATION_CODE_KEY_PREFIX + key;
        return cacheService.get(cacheKey, String.class);
    }

    @Override
    public void evictVerificationCode(String key) {
        String cacheKey = VERIFICATION_CODE_KEY_PREFIX + key;
        cacheService.delete(cacheKey);
        log.debug("清除验证码缓存: key={}", key);
    }

    // ==================== 通用操作 ====================

    @Override
    public void evictUserAllCache(Long userId) {
        evictUserInfo(userId);
        evictUserPermissions(userId);
        evictUserMenus(userId);
        log.info("清除用户所有缓存: userId={}", userId);
    }

    @Override
    public Boolean hasCache(String key) {
        return cacheService.hasKey(key);
    }

    @Override
    public void removeVerificationCode(String email) {
        evictVerificationCode(email);
    }

    @Override
    public void storeVerificationCode(String email, String code) {
        cacheVerificationCode(email, code, Duration.ofMinutes(5)); // 5分钟过期
    }

    // ==================== 认证相关缓存 ====================

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";
    private static final String PASSWORD_RESET_TOKEN_KEY_PREFIX = "auth:reset_token:";
    private static final String CURRENT_ACCOUNT_KEY_PREFIX = "auth:current_account:";

    @Override
    public void storeRefreshToken(String refreshToken, String account) {
        String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        cacheService.set(key, account, Duration.ofDays(SystemCacheConstant.REFRESH_TOKEN_EXPIRE_DAYS));
        log.debug("存储刷新令牌: refreshToken={}, account={}", refreshToken, account);
    }

    @Override
    public String getAccountByRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        return cacheService.get(key, String.class);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        cacheService.delete(key);
        log.debug("删除刷新令牌: refreshToken={}", refreshToken);
    }

    @Override
    public void storePasswordResetToken(String resetToken, String email) {
        String key = PASSWORD_RESET_TOKEN_KEY_PREFIX + resetToken;
        cacheService.set(key, email, Duration.ofMinutes(SystemCacheConstant.PASSWORD_RESET_TOKEN_EXPIRE_MINUTES));
        log.debug("存储密码重置令牌: resetToken={}, email={}", resetToken, email);
    }

    @Override
    public String getEmailByResetToken(String resetToken) {
        String key = PASSWORD_RESET_TOKEN_KEY_PREFIX + resetToken;
        return cacheService.get(key, String.class);
    }

    @Override
    public void removePasswordResetToken(String resetToken) {
        String key = PASSWORD_RESET_TOKEN_KEY_PREFIX + resetToken;
        cacheService.delete(key);
        log.debug("删除密码重置令牌: resetToken={}", resetToken);
    }

    @Override
    public void storeCurrentAccount(String account) {
        String key = CURRENT_ACCOUNT_KEY_PREFIX + Thread.currentThread().threadId();
        cacheService.set(key, account, Duration.ofHours(SystemCacheConstant.CURRENT_ACCOUNT_EXPIRE_HOURS));
        log.debug("存储当前账号: account={}", account);
    }

    @Override
    public String getCurrentAccount() {
        String key = CURRENT_ACCOUNT_KEY_PREFIX + Thread.currentThread().threadId();
        return cacheService.get(key, String.class);
    }

    // ==================== 用户缓存管理 ====================

    @Override
    public void evictUserCache(Long userId) {
        evictUserInfo(userId);
        evictUserPermissions(userId);
        evictUserMenus(userId);
        log.info("清除指定用户缓存: userId={}", userId);
    }

    @Override
    public void evictAllUserCache() {
        // 删除所有用户相关的缓存
        cacheService.deletePattern(USER_INFO_KEY_PREFIX + "*");
        cacheService.deletePattern(USER_PERMISSIONS_KEY_PREFIX + "*");
        cacheService.deletePattern(USER_MENUS_KEY_PREFIX + "*");
        log.info("清除所有用户缓存");
    }

    @Override
    public void putString(String cacheName, String key, String value) {
        String fullKey = cacheName + ":" + key;
        cacheService.set(fullKey, value, Duration.ofMinutes(SystemCacheConstant.TEMP_DATA_EXPIRE_MINUTES));
        log.debug("存储字符串数据: cacheName={}, key={}, value={}", cacheName, key, value);
    }

    @Override
    public String getString(String cacheName, String key) {
        String fullKey = cacheName + ":" + key;
        return cacheService.get(fullKey, String.class);
    }

    @Override
    public void putObject(String cacheName, String key, Object value) {
        String fullKey = cacheName + ":" + key;
        cacheService.set(fullKey, value, Duration.ofHours(SystemCacheConstant.HOT_DATA_EXPIRE_HOURS));
        log.debug("存储对象数据: cacheName={}, key={}, type={}", cacheName, key, value.getClass().getSimpleName());
    }

    @Override
    public <T> T getObject(String cacheName, String key, Class<T> type) {
        String fullKey = cacheName + ":" + key;
        return cacheService.get(fullKey, type);
    }

    // ==================== 字典选项缓存 ====================

    private static final String DICT_OPTIONS_KEY_PREFIX = "dict:options:";

    @Override
    public void storeDictOptions(String dictTypeCode, Object options) {
        String key = DICT_OPTIONS_KEY_PREFIX + dictTypeCode;
        cacheService.set(key, options, Duration.ofHours(SystemCacheConstant.DICT_DATA_EXPIRE_HOURS));
        log.debug("存储字典选项: dictTypeCode={}", dictTypeCode);
    }

    @Override
    public <T> T getDictOptions(String dictTypeCode, Class<T> type) {
        String key = DICT_OPTIONS_KEY_PREFIX + dictTypeCode;
        return cacheService.get(key, type);
    }

    @Override
    public void removeDictOptions(String dictTypeCode) {
        String key = DICT_OPTIONS_KEY_PREFIX + dictTypeCode;
        cacheService.delete(key);
        log.debug("删除字典选项: dictTypeCode={}", dictTypeCode);
    }

    @Override
    public void removeDictOptionsBatch(java.util.List<String> dictTypeCodes) {
        if (dictTypeCodes != null && !dictTypeCodes.isEmpty()) {
            java.util.List<String> keys = dictTypeCodes.stream()
                    .map(code -> DICT_OPTIONS_KEY_PREFIX + code)
                    .collect(java.util.stream.Collectors.toList());
            cacheService.delete(keys);
            log.debug("批量删除字典选项: count={}", dictTypeCodes.size());
        }
    }
}
