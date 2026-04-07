package com.rymcu.mortise.wechat.facade;

import com.rymcu.mortise.wechat.entity.TemplateMessage;
import lombok.Data;

import java.util.Map;

/**
 * 微信消息推送门面
 *
 * @author ronger
 */
public interface WeChatMessageFacade {

    Map<String, String> sendTemplateMessage(Long accountId, TemplateMessage message);

    Map<String, String> sendTextMessage(Long accountId, String openId, String content);

    Map<String, String> sendNewsMessage(Long accountId, NewsMessageRequest request);

    /**
     * 图文消息请求对象
     */
    @Data
    class NewsMessageRequest {
        private String openId;
        private String title;
        private String description;
        private String url;
        private String picUrl;
    }
}
