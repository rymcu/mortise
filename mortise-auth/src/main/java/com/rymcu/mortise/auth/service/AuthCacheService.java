package com.rymcu.mortise.auth.service;

/**
 * 认证缓存服务接口
 * <p>
 * 提供 JWT Token、OAuth2 授权请求等认证相关的缓存操作
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 1.0.0
 */
public interface AuthCacheService {

    // ==================== JWT Token 缓存操作 ====================

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
     * @return 是否删除成功
     */
    Boolean removeJwtToken(String account);

    // ==================== OAuth2 授权请求缓存操作 ====================

    /**
     * 存储 OAuth2 授权请求
     *
     * @param state                OAuth2 state 参数
     * @param authorizationRequest OAuth2 授权请求对象
     */
    void storeOAuth2AuthorizationRequest(String state, Object authorizationRequest);

    /**
     * 获取 OAuth2 授权请求
     *
     * @param state OAuth2 state 参数
     * @param clazz 授权请求对象类型
     * @param <T>   授权请求对象泛型
     * @return OAuth2 授权请求对象，如果不存在返回null
     */
    <T> T getOAuth2AuthorizationRequest(String state, Class<T> clazz);

    /**
     * 删除 OAuth2 授权请求
     *
     * @param state OAuth2 state 参数
     */
    void removeOAuth2AuthorizationRequest(String state);

    /**
     * 存储 OAuth2 参数
     *
     * @param state                OAuth2 state 参数
     * @param parameterMap OAuth2 参数对象
     */
    void storeOAuth2ParameterMap(String state, Object parameterMap);

    /**
     * 获取 OAuth2 参数
     *
     * @param state OAuth2 state 参数
     * @param clazz 参数对象类型
     * @param <T>   参数对象泛型
     * @return OAuth2 参数对象，如果不存在返回null
     */
    <T> T getOAuth2ParameterMap(String state, Class<T> clazz);

    /**
     * 删除 OAuth2 授权请求
     *
     * @param state OAuth2 state 参数
     */
    void removeOAuth2ParameterMap(String state);

    void storeOAuth2QrcodeState(String state, Integer qrcodeState);

    Integer getOAuth2QrcodeState(String state);

    void removeOAuth2QrcodeState(String state);

    // ==================== OAuth2 登录响应缓存操作 ====================

    /**
     * 存储 OAuth2 登录响应（用于 state 兑换 token）
     *
     * @param state         OAuth2 state 参数
     * @param loginResponse OAuth2 登录响应对象
     */
    void storeOAuth2LoginResponse(String state, Object loginResponse);

    /**
     * 获取 OAuth2 登录响应
     *
     * @param state OAuth2 state 参数
     * @param clazz 登录响应对象类型
     * @param <T>   登录响应对象泛型
     * @return OAuth2 登录响应对象，如果不存在返回null
     */
    <T> T getOAuth2LoginResponse(String state, Class<T> clazz);

    /**
     * 删除 OAuth2 登录响应
     *
     * @param state OAuth2 state 参数
     */
    void removeOAuth2LoginResponse(String state);

    // ==================== 会员 Refresh Token 缓存操作 ====================

    /**
     * 存储会员刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @param memberId     会员ID
     */
    void storeMemberRefreshToken(String refreshToken, Long memberId);

    /**
     * 根据刷新令牌获取会员ID
     *
     * @param refreshToken 刷新令牌
     * @return 会员ID，如果不存在返回null
     */
    Long getMemberIdByRefreshToken(String refreshToken);

    /**
     * 删除会员刷新令牌
     *
     * @param refreshToken 刷新令牌
     */
    void removeMemberRefreshToken(String refreshToken);

    // ==================== Token 黑名单操作 ====================

    /**
     * 将 Token 加入黑名单
     * <p>
     * 用于实现 Token 注销功能，被加入黑名单的 Token 即使未过期也将被拒绝
     *
     * @param jti             Token 的唯一标识符 (JWT ID)
     * @param expireInSeconds 黑名单过期时间（秒），通常设置为 Token 剩余有效期
     */
    void addToBlacklist(String jti, long expireInSeconds);

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param jti Token 的唯一标识符 (JWT ID)
     * @return true: 在黑名单中（已注销），false: 不在黑名单中
     */
    boolean isBlacklisted(String jti);
}
