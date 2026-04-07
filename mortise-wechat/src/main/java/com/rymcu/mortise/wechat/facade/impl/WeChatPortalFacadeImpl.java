package com.rymcu.mortise.wechat.facade.impl;

import com.rymcu.mortise.wechat.facade.WeChatPortalFacade;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 微信公众号消息回调门面实现
 *
 * @author ronger
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeChatPortalFacadeImpl implements WeChatPortalFacade {

    private final DynamicWeChatServiceManager dynamicWeChatServiceManager;
    private final Optional<WxMpMessageRouter> messageRouter;

    @Override
    public String authGet(String appid, String signature, String timestamp, String nonce, String echostr) {
        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature, timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }
        WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAppId(appid);
        if (!wxMpService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "非法请求";
    }

    @Override
    public String post(String appid, String requestBody, String signature, String timestamp, String nonce,
                       String openid, String encType, String msgSignature) {
        log.info("""
                        
                        接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],\
                         timestamp=[{}], nonce=[{}], requestBody=[
                        {}
                        ]\s""",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);
        WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAppId(appid);

        if (!wxMpService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxMpService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
        }

        log.debug("\n组装回复信息：{}", out);
        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.messageRouter
                    .orElseThrow(() -> new IllegalStateException("微信消息路由器未初始化"))
                    .route(message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }

        return null;
    }
}
