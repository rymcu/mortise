package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.web.annotation.ApiController;
import com.rymcu.mortise.wechat.facade.WeChatPortalFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 微信公众号回调处理器
 *
 * @author ronger
 */
@AllArgsConstructor
@ApiController
@RequestMapping("/wechat/portal/{appid}")
@Tag(name = "微信Portal", description = "微信公众号消息回调处理接口")
public class WeChatPortalController {
    private final WeChatPortalFacade weChatPortalFacade;

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
        return weChatPortalFacade.authGet(appid, signature, timestamp, nonce, echostr);
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
        return weChatPortalFacade.post(appid, requestBody, signature, timestamp, nonce, openid, encType, msgSignature);
    }

}

