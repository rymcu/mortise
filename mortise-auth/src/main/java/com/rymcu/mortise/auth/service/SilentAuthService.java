package com.rymcu.mortise.auth.service;

/**
 * 静默授权服务接口（SPI）
 * <p>
 * 用于微信 snsapi_base 静默授权场景，绕开 Spring Security OAuth2 登录流程，
 * 仅获取 openid，不执行登录/绑定操作。
 * <p>
 * 接口定义在 auth 模块，由 wechat 模块提供实现（与 {@link QRCodeLoginService} 同模式）。
 *
 * @author ronger
 * @since 1.0.0
 */
public interface SilentAuthService {

    /**
     * 构建静默授权 URL（snsapi_base）
     * <p>
     * 生成微信静默授权链接，redirect_uri 指向前端页面（而非 Spring Security 回调），
     * 微信会直接将 code 回调给前端，不经过 Spring Security OAuth2 登录流程。
     *
     * @param appId       微信公众号 appId，为空时使用默认账号
     * @param redirectUri 授权回调地址（前端页面 URL）
     * @param state       自定义状态参数，用于防 CSRF
     * @return 静默授权 URL
     */
    String buildSilentAuthUrl(String appId, String redirectUri, String state);

    /**
     * 通过授权码静默获取 openid（snsapi_base 场景）
     * <p>
     * 仅执行 code 换 access_token，openid 包含在 token 响应中，
     * 不调用 getUserInfo 接口，不执行登录/绑定。
     *
     * @param appId 微信公众号 appId，为空时使用默认账号
     * @param code  微信授权码
     * @return openid
     */
    String getOpenIdByCode(String appId, String code);
}
