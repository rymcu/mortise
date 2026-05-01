package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.wechat.entity.TemplateMessage;
import com.rymcu.mortise.wechat.facade.WeChatMessageFacade;
import com.rymcu.mortise.wechat.facade.WeChatMessageFacade.NewsMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信消息推送控制器
 *
 * @author ronger
 * @since 1.0.0
 */
@AdminController
@RequestMapping("/wechat/messages")
@RequiredArgsConstructor
@ConditionalOnBean(WeChatMessageFacade.class)
@Tag(name = "微信消息", description = "微信消息发送相关接口")
public class WeChatMessageController {

    private final WeChatMessageFacade weChatMessageFacade;

    /**
     * 发送模板消息
     *
     * @param message   模板消息对象
     * @param accountId 账号ID（可选，不传则使用默认账号）
     * @return 消息 ID
     */
    @Operation(summary = "发送模板消息", description = "发送微信模板消息给指定用户")
    @PostMapping("/template")
    @PreAuthorize("hasAuthority('wechat:message:send')")
    public GlobalResult<Map<String, String>> sendTemplateMessage(
            @Parameter(description = "模板消息对象", required = true)
            @RequestBody TemplateMessage message,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {
        return GlobalResult.success(weChatMessageFacade.sendTemplateMessage(accountId, message));
    }

    /**
     * 发送文本消息
     *
     * @param openId    用户 OpenID
     * @param content   消息内容
     * @param accountId 账号ID（可选，不传则使用默认账号）
     * @return 发送结果
     */
    @Operation(summary = "发送文本消息", description = "发送客服文本消息给指定用户")
    @PostMapping("/text")
    @PreAuthorize("hasAuthority('wechat:message:send')")
    public GlobalResult<Map<String, String>> sendTextMessage(
            @Parameter(description = "用户OpenID", required = true)
            @RequestParam String openId,
            @Parameter(description = "消息内容", required = true)
            @RequestParam String content,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {
        return GlobalResult.success(weChatMessageFacade.sendTextMessage(accountId, openId, content));
    }

    /**
     * 发送图文消息
     *
     * @param request   图文消息请求
     * @param accountId 账号ID（可选，不传则使用默认账号）
     * @return 发送结果
     */
    @Operation(summary = "发送图文消息", description = "发送客服图文消息给指定用户")
    @PostMapping("/news")
    @PreAuthorize("hasAuthority('wechat:message:send')")
    public GlobalResult<Map<String, String>> sendNewsMessage(
            @Parameter(description = "图文消息请求对象", required = true)
            @RequestBody NewsMessageRequest request,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {
        return GlobalResult.success(weChatMessageFacade.sendNewsMessage(accountId, request));
    }
}

