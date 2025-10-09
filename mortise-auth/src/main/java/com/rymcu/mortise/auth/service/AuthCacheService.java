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
}
