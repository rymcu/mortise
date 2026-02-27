package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.system.model.TokenUser;
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

    // ==================== 用户相关缓存 ====================

    @Override
    public void cacheUserInfo(Long userId, Object userInfo) {
        cacheService.set(SystemCacheConstant.USER_INFO_CACHE, String.valueOf(userId), userInfo,
                Duration.ofHours(SystemCacheConstant.USER_INFO_EXPIRE_HOURS));
        log.debug("缓存用户信息: userId={}", userId);
    }

    @Override
    public <T> T getUserInfo(Long userId, Class<T> type) {
        return cacheService.get(SystemCacheConstant.USER_INFO_CACHE, String.valueOf(userId), type);
    }

    @Override
    public void evictUserInfo(Long userId) {
        cacheService.delete(SystemCacheConstant.USER_INFO_CACHE, String.valueOf(userId));
        log.debug("清除用户信息缓存: userId={}", userId);
    }

    @Override
    public void cacheUserPermissions(Long userId, Set<String> permissions) {
        cacheService.set(SystemCacheConstant.USER_PERMISSIONS_CACHE, String.valueOf(userId), permissions,
                Duration.ofMinutes(SystemCacheConstant.USER_PERMISSIONS_EXPIRE_MINUTES));
        log.debug("缓存用户权限: userId={}, permissions={}", userId, permissions.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(Long userId) {
        return cacheService.get(SystemCacheConstant.USER_PERMISSIONS_CACHE, String.valueOf(userId), Set.class);
    }

    @Override
    public void evictUserPermissions(Long userId) {
        cacheService.delete(SystemCacheConstant.USER_PERMISSIONS_CACHE, String.valueOf(userId));
        log.debug("清除用户权限缓存: userId={}", userId);
    }

    // ==================== 字典相关缓存 ====================

    @Override
    public void cacheDictData(String dictType, Object dictData) {
        cacheService.set(SystemCacheConstant.DICT_DATA_CACHE, dictType, dictData,
                Duration.ofHours(SystemCacheConstant.DICT_DATA_EXPIRE_HOURS));
        log.debug("缓存字典数据: dictType={}", dictType);
    }

    @Override
    public <T> T getDictData(String dictType, Class<T> type) {
        return cacheService.get(SystemCacheConstant.DICT_DATA_CACHE, dictType, type);
    }

    @Override
    public void evictDictData(String dictType) {
        cacheService.delete(SystemCacheConstant.DICT_DATA_CACHE, dictType);
        log.debug("清除字典数据缓存: dictType={}", dictType);
    }

    @Override
    public void evictAllDictData() {
        String pattern = SystemCacheConstant.DICT_DATA_CACHE + ":*";
        cacheService.deletePattern(pattern);
        log.info("清除所有字典数据缓存");
    }

    // ==================== 菜单相关缓存 ====================

    @Override
    public void cacheUserMenus(Long userId, Object menus) {
        cacheService.set(SystemCacheConstant.USER_MENU_CACHE, String.valueOf(userId), menus,
                Duration.ofHours(SystemCacheConstant.MENU_DATA_EXPIRE_HOURS));
        log.debug("缓存用户菜单: userId={}", userId);
    }

    @Override
    public <T> T getUserMenus(Long userId, Class<T> type) {
        return cacheService.get(SystemCacheConstant.USER_MENU_CACHE, String.valueOf(userId), type);
    }

    @Override
    public void evictUserMenus(Long userId) {
        cacheService.delete(SystemCacheConstant.USER_MENU_CACHE, String.valueOf(userId));
        log.debug("清除用户菜单缓存: userId={}", userId);
    }

    // ==================== 配置相关缓存 ====================

    @Override
    public void cacheSystemConfig(String configKey, Object configValue) {
        cacheService.set(SystemCacheConstant.SYSTEM_CONFIG_CACHE, configKey, configValue,
                Duration.ofHours(SystemCacheConstant.SYSTEM_CONFIG_EXPIRE_HOURS));
        log.debug("缓存系统配置: configKey={}", configKey);
    }

    @Override
    public <T> T getSystemConfig(String configKey, Class<T> type) {
        return cacheService.get(SystemCacheConstant.SYSTEM_CONFIG_CACHE, configKey, type);
    }

    @Override
    public void evictSystemConfig(String configKey) {
        cacheService.delete(SystemCacheConstant.SYSTEM_CONFIG_CACHE, configKey);
        log.debug("清除系统配置缓存: configKey={}", configKey);
    }

    // ==================== 验证码相关缓存 ====================

    @Override
    public void cacheVerificationCode(String key, String code, Duration timeout) {
        cacheService.set(SystemCacheConstant.VERIFICATION_CODE_CACHE, key, code, timeout);
        log.debug("缓存验证码: key={}, timeout={}", key, timeout);
    }

    @Override
    public String getVerificationCode(String key) {
        return cacheService.get(SystemCacheConstant.VERIFICATION_CODE_CACHE, key, String.class);
    }

    @Override
    public void evictVerificationCode(String key) {
        cacheService.delete(SystemCacheConstant.VERIFICATION_CODE_CACHE, key);
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
    public void storeStandardOAuth2UserInfo(String key, StandardOAuth2UserInfo userInfo) {
        cacheService.set(SystemCacheConstant.STANDARD_OAUTH2_USER_INFO, key, userInfo, Duration.ofMinutes(SystemCacheConstant.STANDARD_OAUTH2_USER_INFO_EXPIRE_MINUTES));
    }

    @Override
    public void storeTokenUser(String state, TokenUser tokenUser) {
        cacheService.set(SystemCacheConstant.STANDARD_AUTH_TOKEN_USER, state, tokenUser, Duration.ofMinutes(SystemCacheConstant.STANDARD_AUTH_TOKEN_USER_EXPIRE_MINUTES));
    }

    @Override
    public TokenUser getOauth2TokenUser(String state) {
        TokenUser user = cacheService.get(SystemCacheConstant.STANDARD_AUTH_TOKEN_USER, state, TokenUser.class);
        if (user != null) {
            cacheService.delete(SystemCacheConstant.STANDARD_AUTH_TOKEN_USER, state);
        }
        return user;
    }

    @Override
    public void removeVerificationCode(String email) {
        evictVerificationCode(email);
    }

    @Override
    public void storeVerificationCode(String email, String code) {
        cacheVerificationCode(email, code, Duration.ofMinutes(SystemCacheConstant.VERIFICATION_CODE_EXPIRE_MINUTES));
    }

    // ==================== 认证相关缓存 ====================

    @Override
    public void storeRefreshToken(String refreshToken, String account) {
        cacheService.set(SystemCacheConstant.REFRESH_TOKEN_CACHE, refreshToken, account,
                Duration.ofDays(SystemCacheConstant.REFRESH_TOKEN_EXPIRE_DAYS));
        log.debug("存储刷新令牌: refreshToken={}, account={}", refreshToken, account);
    }

    @Override
    public String getAccountByRefreshToken(String refreshToken) {
        return cacheService.get(SystemCacheConstant.REFRESH_TOKEN_CACHE, refreshToken, String.class);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        cacheService.delete(SystemCacheConstant.REFRESH_TOKEN_CACHE, refreshToken);
        log.debug("删除刷新令牌: refreshToken={}", refreshToken);
    }

    @Override
    public void storePasswordResetToken(String resetToken, String email) {
        cacheService.set(SystemCacheConstant.PASSWORD_RESET_TOKEN_CACHE, resetToken, email,
                Duration.ofMinutes(SystemCacheConstant.PASSWORD_RESET_TOKEN_EXPIRE_MINUTES));
        log.debug("存储密码重置令牌: resetToken={}, email={}", resetToken, email);
    }

    @Override
    public String getEmailByResetToken(String resetToken) {
        return cacheService.get(SystemCacheConstant.PASSWORD_RESET_TOKEN_CACHE, resetToken, String.class);
    }

    @Override
    public void removePasswordResetToken(String resetToken) {
        cacheService.delete(SystemCacheConstant.PASSWORD_RESET_TOKEN_CACHE, resetToken);
        log.debug("删除密码重置令牌: resetToken={}", resetToken);
    }

    @Override
    public void storeCurrentAccount(String account) {
        cacheService.set(SystemCacheConstant.CURRENT_ACCOUNT_CACHE, String.valueOf(Thread.currentThread().threadId()),
                account, Duration.ofHours(SystemCacheConstant.CURRENT_ACCOUNT_EXPIRE_HOURS));
        log.debug("存储当前账号: account={}", account);
    }

    @Override
    public String getCurrentAccount() {
        return cacheService.get(SystemCacheConstant.CURRENT_ACCOUNT_CACHE, String.valueOf(Thread.currentThread().threadId()),
                String.class);
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
        cacheService.deletePattern(SystemCacheConstant.USER_INFO_CACHE + ":*");
        cacheService.deletePattern(SystemCacheConstant.USER_PERMISSIONS_CACHE + ":*");
        cacheService.deletePattern(SystemCacheConstant.USER_MENU_CACHE + ":*");
        log.info("清除所有用户缓存");
    }

    @Override
    public void putString(String cacheName, String key, String value) {
        cacheService.set(cacheName, key, value, Duration.ofMinutes(SystemCacheConstant.TEMP_DATA_EXPIRE_MINUTES));
        log.debug("存储字符串数据: cacheName={}, key={}, value={}", cacheName, key, value);
    }

    @Override
    public String getString(String cacheName, String key) {
        return cacheService.get(cacheName, key, String.class);
    }

    @Override
    public void putObject(String cacheName, String key, Object value) {
        cacheService.set(cacheName, key, value, Duration.ofHours(SystemCacheConstant.HOT_DATA_EXPIRE_HOURS));
        log.debug("存储对象数据: cacheName={}, key={}, type={}", cacheName, key, value.getClass().getSimpleName());
    }

    @Override
    public <T> T getObject(String cacheName, String key, Class<T> type) {
        return cacheService.get(cacheName, key, type);
    }

    // ==================== 字典选项缓存 ====================

    @Override
    public void storeDictOptions(String dictTypeCode, Object options) {
        cacheService.set(SystemCacheConstant.DICT_OPTIONS_CACHE, dictTypeCode, options,
                Duration.ofHours(SystemCacheConstant.DICT_OPTIONS_EXPIRE_HOURS));
        log.debug("存储字典选项: dictTypeCode={}", dictTypeCode);
    }

    @Override
    public <T> T getDictOptions(String dictTypeCode, Class<T> type) {
        return cacheService.get(SystemCacheConstant.DICT_OPTIONS_CACHE, dictTypeCode, type);
    }

    @Override
    public void removeDictOptions(String dictTypeCode) {
        cacheService.delete(SystemCacheConstant.DICT_OPTIONS_CACHE, dictTypeCode);
        log.debug("删除字典选项: dictTypeCode={}", dictTypeCode);
    }

    @Override
    public void removeDictOptionsBatch(java.util.List<String> dictTypeCodes) {
        if (dictTypeCodes != null && !dictTypeCodes.isEmpty()) {
            java.util.List<String> keys = dictTypeCodes.stream()
                    .map(code -> SystemCacheConstant.DICT_OPTIONS_CACHE + ":" + code)
                    .collect(java.util.stream.Collectors.toList());
            cacheService.delete(keys);
            log.debug("批量删除字典选项: count={}", dictTypeCodes.size());
        }
    }

    // ==================== Dashboard 统计缓存 ====================

    @Override
    public void cacheUserCount(Long count) {
        cacheService.set(SystemCacheConstant.DASHBOARD_USER_COUNT, "value", count,
                Duration.ofHours(SystemCacheConstant.DASHBOARD_STATS_EXPIRE_HOURS));
        log.debug("缓存用户数统计: count={}", count);
    }

    @Override
    public Long getUserCount() {
        Number count = cacheService.get(SystemCacheConstant.DASHBOARD_USER_COUNT, "value", Number.class);
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public void cacheRoleCount(Long count) {
        cacheService.set(SystemCacheConstant.DASHBOARD_ROLE_COUNT, "value", count,
                Duration.ofHours(SystemCacheConstant.DASHBOARD_STATS_EXPIRE_HOURS));
        log.debug("缓存角色数统计: count={}", count);
    }

    @Override
    public Long getRoleCount() {
        Number count = cacheService.get(SystemCacheConstant.DASHBOARD_ROLE_COUNT, "value", Number.class);
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public void cacheMenuCount(Long count) {
        cacheService.set(SystemCacheConstant.DASHBOARD_MENU_COUNT, "value", count,
                Duration.ofHours(SystemCacheConstant.DASHBOARD_STATS_EXPIRE_HOURS));
        log.debug("缓存菜单数统计: count={}", count);
    }

    @Override
    public Long getMenuCount() {
        Number count = cacheService.get(SystemCacheConstant.DASHBOARD_MENU_COUNT, "value", Number.class);
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public void cacheMemberCount(Long count) {
        cacheService.set(SystemCacheConstant.DASHBOARD_MEMBER_COUNT, "value", count,
                Duration.ofHours(SystemCacheConstant.DASHBOARD_STATS_EXPIRE_HOURS));
        log.debug("缓存会员数统计: count={}", count);
    }

    @Override
    public Long getMemberCount() {
        Number count = cacheService.get(SystemCacheConstant.DASHBOARD_MEMBER_COUNT, "value", Number.class);
        return count != null ? count.longValue() : 0L;
    }
}
