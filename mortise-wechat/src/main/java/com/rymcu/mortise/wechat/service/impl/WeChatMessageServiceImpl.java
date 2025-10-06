package com.rymcu.mortise.wechat.service.impl;

import com.rymcu.mortise.wechat.entity.TemplateMessage;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import com.rymcu.mortise.wechat.service.WeChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信消息服务实现类
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatMessageServiceImpl implements WeChatMessageService {

    private final DynamicWeChatServiceManager dynamicWeChatServiceManager;

    @Override
    public String sendTemplateMessage(Long accountId, TemplateMessage message) throws WxErrorException {
        log.info("发送模板消息 - accountId: {}, toUser: {}, templateId: {}",
                accountId, maskString(message.getToUser()), message.getTemplateId());

        WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAccountId(accountId);
        if (wxMpService == null) {
            throw new RuntimeException("未找到可用的微信公众号服务");
        }

        // 构建模板消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(message.getToUser())
                .templateId(message.getTemplateId())
                .url(message.getUrl())
                .data(convertTemplateData(message.getData()))
                .build();

        // 发送消息
        String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        log.info("模板消息发送成功 - msgId: {}", msgId);

        return msgId;
    }

    @Override
    public void sendTextMessage(Long accountId, String openId, String content) throws WxErrorException {
        log.info("发送文本消息 - accountId: {}, openId: {}, content length: {}",
                accountId, maskString(openId), content != null ? content.length() : 0);

        WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAccountId(accountId);
        if (wxMpService == null) {
            throw new RuntimeException("未找到可用的微信公众号服务");
        }

        // 发送客服文本消息
        wxMpService.getKefuService().sendKefuMessage(
                me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage
                        .TEXT()
                        .toUser(openId)
                        .content(content)
                        .build()
        );

        log.info("文本消息发送成功");
    }

    @Override
    public void sendNewsMessage(Long accountId, String openId, String title,
                               String description, String url, String picUrl) throws WxErrorException {
        log.info("发送图文消息 - accountId: {}, openId: {}, title: {}",
                accountId, maskString(openId), title);

        WxMpService wxMpService = dynamicWeChatServiceManager.getServiceByAccountId(accountId);
        if (wxMpService == null) {
            throw new RuntimeException("未找到可用的微信公众号服务");
        }

        // 构建图文消息的article
        me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage.WxArticle article =
                new me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage.WxArticle();
        article.setTitle(title);
        article.setDescription(description);
        article.setUrl(url);
        article.setPicUrl(picUrl);

        // 构建图文消息
        me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage newsMessage =
                me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage.NEWS()
                        .toUser(openId)
                        .addArticle(article)
                        .build();

        // 发送消息
        wxMpService.getKefuService().sendKefuMessage(newsMessage);
        log.info("图文消息发送成功");
    }

    @Override
    @CacheEvict(value = "wechat:message", allEntries = true)
    public void refreshCache() {
        log.info("刷新微信消息缓存");
    }

    /**
     * 转换模板数据格式
     */
    private List<WxMpTemplateData> convertTemplateData(List<TemplateMessage.TemplateData> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }

        return data.stream()
                .map(d -> new WxMpTemplateData(d.getName(), d.getValue(), d.getColor()))
                .collect(Collectors.toList());
    }

    /**
     * 脱敏字符串（用于日志）
     */
    private String maskString(String str) {
        if (str == null || str.length() <= 8) {
            return "***";
        }
        return str.substring(0, 4) + "***" + str.substring(str.length() - 4);
    }
}
