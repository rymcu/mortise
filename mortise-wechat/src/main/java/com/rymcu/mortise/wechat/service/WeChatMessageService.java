package com.rymcu.mortise.wechat.service;

import com.rymcu.mortise.wechat.entity.TemplateMessage;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * 微信消息服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface WeChatMessageService {

    /**
     * 发送模板消息
     *
     * @param accountId 账号ID（可选，不传则使用默认公众号账号）
     * @param message   模板消息对象
     * @return 消息 ID
     * @throws WxErrorException 微信API异常
     */
    String sendTemplateMessage(Long accountId, TemplateMessage message) throws WxErrorException;

    /**
     * 发送文本消息
     *
     * @param accountId 账号ID（可选，不传则使用默认公众号账号）
     * @param openId    用户 OpenID
     * @param content   消息内容
     * @throws WxErrorException 微信API异常
     */
    void sendTextMessage(Long accountId, String openId, String content) throws WxErrorException;

    /**
     * 发送图文消息
     *
     * @param accountId   账号ID（可选，不传则使用默认公众号账号）
     * @param openId      用户 OpenID
     * @param title       标题
     * @param description 描述
     * @param url         跳转链接
     * @param picUrl      图片链接
     * @throws WxErrorException 微信API异常
     */
    void sendNewsMessage(Long accountId, String openId, String title, 
                        String description, String url, String picUrl) throws WxErrorException;

    /**
     * 刷新缓存（清除所有消息相关缓存）
     */
    void refreshCache();
}
