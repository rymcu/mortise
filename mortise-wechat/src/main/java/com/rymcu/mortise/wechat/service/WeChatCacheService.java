package com.rymcu.mortise.wechat.service;

/**
 * 微信缓存服务接口
 * <p>封装微信模块的缓存操作，避免直接依赖基础设施层的 CacheService</p>
 *
 * @author ronger
 * @since 1.0.0
 */
public interface WeChatCacheService {

    /**
     * 缓存授权 State
     *
     * @param state     State 字符串
     * @param accountId 账号ID（null 表示默认账号）
     */
    void cacheAuthState(String state, Long accountId);

    /**
     * 验证并获取 State 对应的 AccountId
     * <p>验证成功后会自动删除缓存（防止重放攻击）</p>
     *
     * @param state State 字符串
     * @return 账号ID（null 表示使用默认账号）
     * @throws IllegalStateException State 无效或已过期
     */
    Long validateAndGetAccountId(String state);

    /**
     * 缓存微信用户信息
     *
     * @param openId   用户 OpenID
     * @param userInfo 用户信息
     * @param minutes  过期时间（分钟）
     */
    void cacheUserInfo(String openId, Object userInfo, long minutes);

    /**
     * 获取缓存的用户信息
     *
     * @param openId 用户 OpenID
     * @param type   返回类型
     * @param <T>    泛型类型
     * @return 用户信息（不存在则返回 null）
     */
    <T> T getUserInfo(String openId, Class<T> type);

    /**
     * 删除用户信息缓存
     *
     * @param openId 用户 OpenID
     */
    void deleteUserInfo(String openId);

    /**
     * 缓存访问令牌
     *
     * @param accountId   账号ID
     * @param accessToken 访问令牌
     * @param expiresIn   有效期（秒）
     */
    void cacheAccessToken(Long accountId, String accessToken, int expiresIn);

    /**
     * 获取访问令牌
     *
     * @param accountId 账号ID
     * @return 访问令牌（不存在则返回 null）
     */
    String getAccessToken(Long accountId);

    /**
     * 删除访问令牌
     *
     * @param accountId 账号ID
     */
    void deleteAccessToken(Long accountId);
}
