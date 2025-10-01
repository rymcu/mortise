package com.rymcu.mortise.system.service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * 系统缓存服务接口
 * 业务层封装 - 封装基础设施层的 CacheService
 * 
 * 设计原则：
 * - 业务模块不直接调用 CacheService
 * - 通过 SystemCacheService 提供业务语义化的缓存操作
 * - 内部使用 CacheService 实现
 *
 * @author ronger
 */
public interface SystemCacheService {

    // ==================== 用户相关缓存 ====================

    /**
     * 缓存用户信息
     *
     * @param userId 用户ID
     * @param userInfo 用户信息
     */
    void cacheUserInfo(Long userId, Object userInfo);

    /**
     * 获取用户信息缓存
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    <T> T getUserInfo(Long userId, Class<T> type);

    /**
     * 删除用户信息缓存
     *
     * @param userId 用户ID
     */
    void evictUserInfo(Long userId);

    /**
     * 缓存用户权限
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     */
    void cacheUserPermissions(Long userId, Set<String> permissions);

    /**
     * 获取用户权限缓存
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 删除用户权限缓存
     *
     * @param userId 用户ID
     */
    void evictUserPermissions(Long userId);

    // ==================== 字典相关缓存 ====================

    /**
     * 缓存字典数据
     *
     * @param dictType 字典类型
     * @param dictData 字典数据
     */
    void cacheDictData(String dictType, Object dictData);

    /**
     * 获取字典数据缓存
     *
     * @param dictType 字典类型
     * @return 字典数据
     */
    <T> T getDictData(String dictType, Class<T> type);

    /**
     * 删除字典数据缓存
     *
     * @param dictType 字典类型
     */
    void evictDictData(String dictType);

    /**
     * 清空所有字典缓存
     */
    void evictAllDictData();

    // ==================== 菜单相关缓存 ====================

    /**
     * 缓存用户菜单
     *
     * @param userId 用户ID
     * @param menus 菜单列表
     */
    void cacheUserMenus(Long userId, Object menus);

    /**
     * 获取用户菜单缓存
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    <T> T getUserMenus(Long userId, Class<T> type);

    /**
     * 删除用户菜单缓存
     *
     * @param userId 用户ID
     */
    void evictUserMenus(Long userId);

    // ==================== 配置相关缓存 ====================

    /**
     * 缓存系统配置
     *
     * @param configKey 配置键
     * @param configValue 配置值
     */
    void cacheSystemConfig(String configKey, Object configValue);

    /**
     * 获取系统配置缓存
     *
     * @param configKey 配置键
     * @return 配置值
     */
    <T> T getSystemConfig(String configKey, Class<T> type);

    /**
     * 删除系统配置缓存
     *
     * @param configKey 配置键
     */
    void evictSystemConfig(String configKey);

    // ==================== 验证码相关缓存 ====================

    /**
     * 缓存验证码
     *
     * @param key 验证码键（如邮箱、手机号）
     * @param code 验证码
     * @param timeout 过期时间
     */
    void cacheVerificationCode(String key, String code, Duration timeout);

    /**
     * 获取验证码
     *
     * @param key 验证码键
     * @return 验证码
     */
    String getVerificationCode(String key);

    /**
     * 删除验证码
     *
     * @param key 验证码键
     */
    void evictVerificationCode(String key);

    /**
     * 删除验证码（兼容原方法名）
     *
     * @param email 邮箱
     */
    void removeVerificationCode(String email);

    /**
     * 存储验证码（兼容原方法名）
     *
     * @param email 邮箱
     * @param code 验证码
     */
    void storeVerificationCode(String email, String code);

    // ==================== 认证相关缓存 ====================

    /**
     * 存储刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @param account 账号
     */
    void storeRefreshToken(String refreshToken, String account);

    /**
     * 根据刷新令牌获取账号
     *
     * @param refreshToken 刷新令牌
     * @return 账号
     */
    String getAccountByRefreshToken(String refreshToken);

    /**
     * 删除刷新令牌
     *
     * @param refreshToken 刷新令牌
     */
    void removeRefreshToken(String refreshToken);

    /**
     * 存储密码重置令牌
     *
     * @param resetToken 重置令牌
     * @param email 邮箱
     */
    void storePasswordResetToken(String resetToken, String email);

    /**
     * 根据重置令牌获取邮箱
     *
     * @param resetToken 重置令牌
     * @return 邮箱
     */
    String getEmailByResetToken(String resetToken);

    /**
     * 删除密码重置令牌
     *
     * @param resetToken 重置令牌
     */
    void removePasswordResetToken(String resetToken);

    /**
     * 存储当前账号信息
     *
     * @param account 账号
     */
    void storeCurrentAccount(String account);

    /**
     * 获取当前账号信息
     *
     * @return 账号
     */
    String getCurrentAccount();

    // ==================== 用户缓存管理 ====================

    /**
     * 清除用户缓存
     *
     * @param userId 用户ID
     */
    void evictUserCache(Long userId);

    /**
     * 清除所有用户缓存
     */
    void evictAllUserCache();

    /**
     * 存储字符串数据
     *
     * @param cacheName 缓存名称
     * @param key 键
     * @param value 值
     */
    void putString(String cacheName, String key, String value);

    /**
     * 获取字符串数据
     *
     * @param cacheName 缓存名称
     * @param key 键
     * @return 值
     */
    String getString(String cacheName, String key);

    /**
     * 存储对象数据
     *
     * @param cacheName 缓存名称
     * @param key 键
     * @param value 值
     */
    void putObject(String cacheName, String key, Object value);

    /**
     * 获取对象数据
     *
     * @param cacheName 缓存名称
     * @param key 键
     * @param type 对象类型
     * @return 对象
     */
    <T> T getObject(String cacheName, String key, Class<T> type);

    // ==================== 字典选项缓存 ====================

    /**
     * 存储字典选项
     *
     * @param dictTypeCode 字典类型编码
     * @param options 选项列表
     */
    void storeDictOptions(String dictTypeCode, Object options);

    /**
     * 获取字典选项
     *
     * @param dictTypeCode 字典类型编码
     * @param type 返回类型
     * @return 选项列表
     */
    <T> T getDictOptions(String dictTypeCode, Class<T> type);

    /**
     * 删除字典选项
     *
     * @param dictTypeCode 字典类型编码
     */
    void removeDictOptions(String dictTypeCode);

    /**
     * 批量删除字典选项
     *
     * @param dictTypeCodes 字典类型编码列表
     */
    void removeDictOptionsBatch(java.util.List<String> dictTypeCodes);

    // ==================== 通用操作 ====================

    /**
     * 清空用户相关的所有缓存
     *
     * @param userId 用户ID
     */
    void evictUserAllCache(Long userId);

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    Boolean hasCache(String key);
}
