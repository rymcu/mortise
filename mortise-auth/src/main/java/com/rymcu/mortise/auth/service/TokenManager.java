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

}

