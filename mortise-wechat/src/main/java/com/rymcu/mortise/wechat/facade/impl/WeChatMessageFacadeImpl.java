package com.rymcu.mortise.wechat.facade.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.wechat.entity.TemplateMessage;
import com.rymcu.mortise.wechat.facade.WeChatMessageFacade;
import com.rymcu.mortise.wechat.service.WeChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信消息推送门面实现
 *
 * @author ronger
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(WeChatMessageService.class)
public class WeChatMessageFacadeImpl implements WeChatMessageFacade {

    private final WeChatMessageService weChatMessageService;

    @Override
    public Map<String, String> sendTemplateMessage(Long accountId, TemplateMessage message) {
        try {
            String msgId = weChatMessageService.sendTemplateMessage(accountId, message);

            Map<String, String> result = new HashMap<>();
            result.put("msgId", msgId);
            result.put("status", "success");
            return result;

        } catch (WxErrorException e) {
            log.error("发送模板消息失败", e);
            throw new BusinessException("发送模板消息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> sendTextMessage(Long accountId, String openId, String content) {
        try {
            weChatMessageService.sendTextMessage(accountId, openId, content);

            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            return result;

        } catch (WxErrorException e) {
            log.error("发送文本消息失败", e);
            throw new BusinessException("发送文本消息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> sendNewsMessage(Long accountId, NewsMessageRequest request) {
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
            return result;

        } catch (WxErrorException e) {
            log.error("发送图文消息失败", e);
            throw new BusinessException("发送图文消息失败: " + e.getMessage());
        }
    }
}
