package com.rymcu.mortise.wechat.service;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * 微信登录服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface WeChatLoginService {

    /**
     * 构建微信扫码登录授权 URL（PC 端）
     *
     * @param accountId   账号ID（可选，不传则使用默认公众号账号）
     * @param redirectUri 授权后重定向地址
     * @param state       状态码（用于防止 CSRF 攻击）
     * @return 微信授权 URL
     */
    String buildAuthorizationUrl(Long accountId, String redirectUri, String state);

    /**
     * 构建微信 H5 授权 URL（移动端）
     *
     * @param accountId   账号ID（可选，不传则使用默认公众号账号）
     * @param redirectUri 授权后重定向地址
     * @param state       状态码（用于防止 CSRF 攻击）
     * @return 微信授权 URL
     */
    String buildH5AuthorizationUrl(Long accountId, String redirectUri, String state);

    /**
     * 通过授权码获取用户信息
     *
     * @param accountId 账号ID（可选，不传则使用默认公众号账号）
     * @param code      微信授权码
     * @return 微信用户信息
     * @throws WxErrorException 微信API异常
     */
    WxOAuth2UserInfo getUserInfoByCode(Long accountId, String code) throws WxErrorException;

    /**
     * 验证 Access Token 是否有效
     *
     * @param accessToken Access Token
     * @param openId      用户 OpenID
     * @return 是否有效
     */
    boolean validateAccessToken(String accessToken, String openId);

    /**
     * 构建静默授权 URL（snsapi_base）
     * <p>
     * redirect_uri 指向前端页面，绕开 Spring Security OAuth2 登录流程。
     *
     * @param appId       微信公众号 appId，为空时使用默认账号
     * @param redirectUri 授权回调地址（前端页面 URL）
     * @param state       自定义状态参数
     * @return 静默授权 URL
     */
    String buildSilentAuthUrl(String appId, String redirectUri, String state);

    /**
     * 通过授权码静默获取 openid（snsapi_base 场景）
     * <p>
     * 仅执行 code 换 access_token，openid 包含在 token 响应中，
     * 不调用 getUserInfo 接口，适用于无感获取 openid 的场景。
     *
     * @param appId 微信公众号 appId，为空时使用默认账号
     * @param code  微信授权码
     * @return openid
     */
    String getOpenIdByCode(String appId, String code);

    /**
     * 刷新缓存（清除所有登录相关缓存）
     */
    void refreshCache();
}
