package com.rymcu.mortise.wechat.integration;

import com.rymcu.mortise.wechat.entity.TemplateMessage;
import com.rymcu.mortise.wechat.service.WeChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 微信通知发送器
 * <p>将微信消息推送集成到 mortise-notification 模块</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 1. 在 mortise-notification 模块中定义 NotificationSender 接口
 * // 2. 本类实现该接口
 * // 3. 通过 SPI 机制自动发现和注册
 * </pre>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(WeChatMessageService.class)
public class WeChatNotificationSender {

    private final WeChatMessageService weChatMessageService;

    /**
     * 发送欢迎通知（使用默认账号）
     *
     * @param openId   用户 OpenID
     * @param username 用户名
     * @param time     时间
     */
    public void sendWelcomeNotification(String openId, String username, String time) {
        sendWelcomeNotification(null, openId, username, time);
    }

    /**
     * 发送欢迎通知（指定账号）
     *
     * @param accountId 账号ID（null 表示使用默认公众号账号）
     * @param openId    用户 OpenID
     * @param username  用户名
     * @param time      时间
     */
    public void sendWelcomeNotification(Long accountId, String openId, String username, String time) {
        try {
            TemplateMessage message = TemplateMessage.builder()
                    .toUser(openId)
                    .templateId("welcome-template-id") // 需在公众平台配置
                    .build()
                    .addData("first", "欢迎注册！", "#173177")
                    .addData("keyword1", username)
                    .addData("keyword2", time)
                    .addData("remark", "感谢您的使用");

            weChatMessageService.sendTemplateMessage(accountId, message);
            log.info("欢迎通知发送成功，accountId: {}, openId: {}", accountId, openId);

        } catch (WxErrorException e) {
            log.error("发送欢迎通知失败，accountId: {}, openId: {}", accountId, openId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发送登录通知（使用默认账号）
     *
     * @param openId   用户 OpenID
     * @param ip       登录 IP
     * @param location 登录地点
     * @param device   登录设备
     * @param time     登录时间
     */
    public void sendLoginNotification(String openId, String ip, String location, 
                                     String device, String time) {
        sendLoginNotification(null, openId, ip, location, device, time);
    }

    /**
     * 发送登录通知（指定账号）
     *
     * @param accountId 账号ID（null 表示使用默认公众号账号）
     * @param openId    用户 OpenID
     * @param ip        登录 IP
     * @param location  登录地点
     * @param device    登录设备
     * @param time      登录时间
     */
    public void sendLoginNotification(Long accountId, String openId, String ip, String location, 
                                     String device, String time) {
        try {
            TemplateMessage message = TemplateMessage.builder()
                    .toUser(openId)
                    .templateId("login-notification-template-id")
                    .build()
                    .addData("first", "您的账号有新的登录", "#FF0000")
                    .addData("keyword1", ip)
                    .addData("keyword2", location)
                    .addData("keyword3", device)
                    .addData("keyword4", time)
                    .addData("remark", "如非本人操作，请立即修改密码");

            weChatMessageService.sendTemplateMessage(accountId, message);
            log.info("登录通知发送成功，accountId: {}, openId: {}", accountId, openId);

        } catch (WxErrorException e) {
            log.error("发送登录通知失败，accountId: {}, openId: {}", accountId, openId, e);
        }
    }

    /**
     * 发送系统通知（使用默认账号）
     *
     * @param openId  用户 OpenID
     * @param title   通知标题
     * @param content 通知内容
     * @param time    通知时间
     */
    public void sendSystemNotification(String openId, String title, 
                                      String content, String time) {
        sendSystemNotification(null, openId, title, content, time);
    }

    /**
     * 发送系统通知（指定账号）
     *
     * @param accountId 账号ID（null 表示使用默认公众号账号）
     * @param openId    用户 OpenID
     * @param title     通知标题
     * @param content   通知内容
     * @param time      通知时间
     */
    public void sendSystemNotification(Long accountId, String openId, String title, 
                                      String content, String time) {
        try {
            TemplateMessage message = TemplateMessage.builder()
                    .toUser(openId)
                    .templateId("system-notification-template-id")
                    .build()
                    .addData("first", title, "#173177")
                    .addData("keyword1", content)
                    .addData("keyword2", time)
                    .addData("remark", "感谢您的关注");

            weChatMessageService.sendTemplateMessage(accountId, message);
            log.info("系统通知发送成功，accountId: {}, openId: {}", accountId, openId);

        } catch (WxErrorException e) {
            log.error("发送系统通知失败，accountId: {}, openId: {}", accountId, openId, e);
        }
    }

    /**
     * 发送简单文本消息（使用默认账号）
     *
     * @param openId  用户 OpenID
     * @param content 消息内容
     */
    public void sendTextNotification(String openId, String content) {
        sendTextNotification(null, openId, content);
    }

    /**
     * 发送简单文本消息（指定账号）
     *
     * @param accountId 账号ID（null 表示使用默认公众号账号）
     * @param openId    用户 OpenID
     * @param content   消息内容
     */
    public void sendTextNotification(Long accountId, String openId, String content) {
        try {
            weChatMessageService.sendTextMessage(accountId, openId, content);
            log.info("文本消息发送成功，accountId: {}, openId: {}", accountId, openId);

        } catch (WxErrorException e) {
            log.error("发送文本消息失败，accountId: {}, openId: {}", accountId, openId, e);
        }
    }

    /**
     * 批量发送通知
     *
     * @param openIds 用户 OpenID 列表
     * @param content 消息内容
     */
    public void sendBatchNotification(java.util.List<String> openIds, String content) {
        for (String openId : openIds) {
            sendTextNotification(openId, content);
            
            // 避免触发微信频率限制
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("批量发送被中断");
                break;
            }
        }
        log.info("批量通知发送完成，总数: {}", openIds.size());
    }
}
