package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 微信公众号回调处理器
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 * @author ronger
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/wechat/portal/{appid}")
@Tag(name = "微信Portal", description = "微信公众号消息回调处理接口")
public class WeChatPortalController {
    private final DynamicWeChatServiceManager dynamicWeChatServiceManager; // 微信公众号服务工具类

    private final Optional<WxMpMessageRouter> messageRouter;

    /**
     * 微信服务器认证（GET请求）
     */
    @Operation(summary = "微信服务器认证", description = "处理微信服务器的认证请求")
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(
            @Parameter(description = "公众号AppID", required = true)
            @PathVariable String appid,
            @Parameter(description = "微信加密签名", required = false)
            @RequestParam(name = "signature", required = false) String signature,
            @Parameter(description = "时间戳", required = false)
            @RequestParam(name = "timestamp", required = false) String timestamp,
            @Parameter(description = "随机数", required = false)
            @RequestParam(name = "nonce", required = false) String nonce,
            @Parameter(description = "随机字符串", required = false)
            @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
            timestamp, nonce, echostr);
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

    /**
     * 微信消息回调（POST请求）
     */
    @Operation(summary = "微信消息回调", description = "处理微信服务器推送的消息和事件")
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(
            @Parameter(description = "公众号AppID", required = true)
            @PathVariable String appid,
            @Parameter(description = "消息体XML", required = true)
            @RequestBody String requestBody,
            @Parameter(description = "微信加密签名", required = true)
            @RequestParam("signature") String signature,
            @Parameter(description = "时间戳", required = true)
            @RequestParam("timestamp") String timestamp,
            @Parameter(description = "随机数", required = true)
            @RequestParam("nonce") String nonce,
            @Parameter(description = "用户OpenID", required = true)
            @RequestParam("openid") String openid,
            @Parameter(description = "加密类型", required = false)
            @RequestParam(name = "encrypt_type", required = false) String encType,
            @Parameter(description = "消息签名", required = false)
            @RequestParam(name = "msg_signature", required = false) String msgSignature) {
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
