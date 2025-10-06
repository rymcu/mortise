package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.wechat.entity.TemplateMessage;
import com.rymcu.mortise.wechat.service.WeChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信消息推送控制器
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wechat/messages")
@RequiredArgsConstructor
@ConditionalOnBean(WeChatMessageService.class)
@Tag(name = "微信消息", description = "微信消息发送相关接口")
public class WeChatMessageController {

    private final WeChatMessageService weChatMessageService;

    /**
     * 发送模板消息
     *
     * @param message   模板消息对象
     * @param accountId 账号ID（可选，不传则使用默认账号）
     * @return 消息 ID
     */
    @Operation(summary = "发送模板消息", description = "发送微信模板消息给指定用户")
    @PostMapping("/template")
    public GlobalResult<Map<String, String>> sendTemplateMessage(
            @Parameter(description = "模板消息对象", required = true)
            @RequestBody TemplateMessage message,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {

        try {
            String msgId = weChatMessageService.sendTemplateMessage(accountId, message);

            Map<String, String> result = new HashMap<>();
            result.put("msgId", msgId);
            result.put("status", "success");

            return GlobalResult.success(result);

        } catch (WxErrorException e) {
            log.error("发送模板消息失败", e);
            return GlobalResult.error("发送模板消息失败: " + e.getMessage());
        }
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
    public GlobalResult<Map<String, String>> sendTextMessage(
            @Parameter(description = "用户OpenID", required = true)
            @RequestParam String openId,
            @Parameter(description = "消息内容", required = true)
            @RequestParam String content,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {

        try {
            weChatMessageService.sendTextMessage(accountId, openId, content);

            Map<String, String> result = new HashMap<>();
            result.put("status", "success");

            return GlobalResult.success(result);

        } catch (WxErrorException e) {
            log.error("发送文本消息失败", e);
            return GlobalResult.error("发送文本消息失败: " + e.getMessage());
        }
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
    public GlobalResult<Map<String, String>> sendNewsMessage(
            @Parameter(description = "图文消息请求对象", required = true)
            @RequestBody NewsMessageRequest request,
            @Parameter(description = "账号ID（可选，不传则使用默认账号）")
            @RequestParam(required = false) Long accountId) {

        try {
            weChatMessageService.sendNewsMessage(
                    accountId,
                    request.getOpenId(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getUrl(),
                    request.getPicUrl()
            );

            Map<String, String> result = new HashMap<>();
            result.put("status", "success");

            return GlobalResult.success(result);

        } catch (WxErrorException e) {
            log.error("发送图文消息失败", e);
            return GlobalResult.error("发送图文消息失败: " + e.getMessage());
        }
    }

    /**
     * 图文消息请求对象
     */
    @lombok.Data
    public static class NewsMessageRequest {
        private String openId;
        private String title;
        private String description;
        private String url;
        private String picUrl;
    }
}
