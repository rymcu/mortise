package com.rymcu.mortise.auth.service;

import com.rymcu.mortise.auth.model.TokenModel;

/**
 * 对token进行操作的接口
 *
 * @author ScienJus
 * @date 2015/7/31.
 */
public interface TokenManager {

    /**
     * 创建一个token关联上指定用户
     *
     * @param id
     * @return 生成的token
     */
    String createToken(String id);

    /**
     * 检查token是否有效
     *
     * @param model token
     * @return 是否有效
     */
    boolean checkToken(TokenModel model);

    /**
     * 从字符串中解析token
     *
     * @param token
     * @param account
     * @return
     */
    TokenModel getToken(String token, String account);

    /**
     * 清除token
     *
     * @param account 登录用户账号
     */
    void deleteToken(String account);

    /**
     * 刷新 Access Token
     * 使用 JWT 自带的刷新机制，验证旧 Token 并生成新 Token
     *
     * @param oldToken 旧的 Access Token
     * @param account 用户账号（用于验证）
     * @return 新的 Access Token，如果刷新失败返回 null
     */
    String refreshAccessToken(String oldToken, String account);

}

