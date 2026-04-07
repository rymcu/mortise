package com.rymcu.mortise.wechat.facade;

/**
 * 微信公众号消息回调门面
 *
 * @author ronger
 */
public interface WeChatPortalFacade {

    String authGet(String appid, String signature, String timestamp, String nonce, String echostr);

    String post(String appid, String requestBody, String signature, String timestamp, String nonce,
                String openid, String encType, String msgSignature);
}
