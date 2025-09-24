package com.rymcu.mortise.service;

/**
 * 统一缓存服务接口
 * 提供所有业务模块的缓存操作
 *
 * @author ronger
 */
public interface CacheService {

    /**
     * 存储验证码
     *
     * @param email 邮箱
     * @param code  验证码
     */
    void storeVerificationCode(String email, String code);

    /**
     * 获取验证码
     *
     * @param email 邮箱
     * @return 验证码，如果不存在或已过期返回null
     */
    String getVerificationCode(String email);

    /**
     * 删除验证码
     *
     * @param email 邮箱
     */
    void removeVerificationCode(String email);

    /**
     * 存储刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @param account      用户账号
     */
    void storeRefreshToken(String refreshToken, String account);

    /**
     * 获取刷新令牌对应的账号
     *
     * @param refreshToken 刷新令牌
     * @return 用户账号，如果不存在或已过期返回null
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
     * @param email      邮箱
     */
    void storePasswordResetToken(String resetToken, String email);

    /**
     * 获取密码重置令牌对应的邮箱
     *
     * @param resetToken 重置令牌
     * @return 邮箱，如果不存在或已过期返回null
     */
    String getEmailByResetToken(String resetToken);

    /**
     * 删除密码重置令牌
     *
     * @param resetToken 重置令牌
     */
    void removePasswordResetToken(String resetToken);

    // ========== 通用缓存操作 ==========

    /**
     * 存储字符串值到指定缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     */
    void putString(String cacheName, String key, String value);

    /**
     * 从指定缓存获取字符串值
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 缓存值，如果不存在返回null
     */
    String getString(String cacheName, String key);

    /**
     * 从指定缓存删除值
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    void evict(String cacheName, String key);

    /**
     * 存储对象到指定缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     */
    void putObject(String cacheName, String key, Object value);

    /**
     * 从指定缓存获取对象
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param clazz     对象类型
     * @return 缓存值，如果不存在返回null
     */
    <T> T getObject(String cacheName, String key, Class<T> clazz);

    // ========== 用户相关缓存操作 ==========

    /**
     * 存储当前账号序列
     *
     * @param currentAccount 当前账号
     */
    void storeCurrentAccount(String currentAccount);

    /**
     * 获取当前账号序列
     *
     * @return 当前账号，如果不存在返回null
     */
    String getCurrentAccount();

    /**
     * 清除用户相关缓存
     *
     * @param userId 用户ID
     */
    void evictUserCache(Long userId);

    /**
     * 清除所有用户缓存
     */
    void evictAllUserCache();

    // ========== JWT Token 相关缓存操作 ==========

    /**
     * 存储JWT Token
     *
     * @param account 用户账号
     * @param token   JWT Token
     */
    void storeJwtToken(String account, String token);

    /**
     * 获取JWT Token
     *
     * @param account 用户账号
     * @return JWT Token，如果不存在返回null
     */
    String getJwtToken(String account);

    /**
     * 删除JWT Token
     *
     * @param account 用户账号
     */
    void removeJwtToken(String account);

    /**
     * 存储用户在线状态
     *
     * @param account      用户账号
     * @param lastOnlineTime 最后在线时间
     */
    void storeUserOnlineStatus(String account, String lastOnlineTime);

    /**
     * 获取用户在线状态
     *
     * @param account 用户账号
     * @return 最后在线时间，如果不存在返回null
     */
    String getUserOnlineStatus(String account);

    // ========== OAuth2 相关缓存操作 ==========

    /**
     * 存储OAuth2授权请求
     *
     * @param state             授权状态参数
     * @param authorizationRequest OAuth2授权请求对象
     */
    void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest);

    /**
     * 获取OAuth2授权请求
     *
     * @param state 授权状态参数
     * @param clazz 请求对象类型
     * @return OAuth2授权请求对象，如果不存在返回null
     */
    <T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz);

    /**
     * 删除OAuth2授权请求
     *
     * @param state 授权状态参数
     */
    void removeOAuth2AuthorizationRequest(String state);

    // ========== 字典缓存操作 ==========

    /**
     * 存储字典选项数据
     *
     * @param dictTypeCode 字典类型代码
     * @param options      字典选项列表
     */
    void storeDictOptions(String dictTypeCode, Object options);

    /**
     * 获取字典选项数据
     *
     * @param dictTypeCode 字典类型代码
     * @param clazz        选项对象类型
     * @return 字典选项列表，如果不存在返回null
     */
    <T> T getDictOptions(String dictTypeCode, Class<T> clazz);

    /**
     * 删除字典选项缓存
     *
     * @param dictTypeCode 字典类型代码
     */
    void removeDictOptions(String dictTypeCode);

    /**
     * 删除所有字典相关缓存
     * 用于DictType变更时的全量刷新
     */
    void removeAllDictOptions();

    /**
     * 批量删除指定字典类型的缓存
     *
     * @param dictTypeCodes 字典类型代码列表
     */
    void removeDictOptionsBatch(java.util.List<String> dictTypeCodes);
}