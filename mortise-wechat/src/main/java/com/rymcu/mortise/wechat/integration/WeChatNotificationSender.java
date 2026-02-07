package com.rymcu.mortise.wechat.integration;

import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.spi.NotificationSender;
import com.rymcu.mortise.wechat.entity.TemplateMessage;
import com.rymcu.mortise.wechat.service.WeChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信通知发送器 — 实现 NotificationSender SPI
 * <p>
 * 通过 SPI 机制自动注册到 NotificationServiceImpl，
 * 使 notification 模块无需依赖 wechat 模块即可分发微信通知。
 * </p>
 * <p>
 * NotificationMessage.metadata 约定：
 * <ul>
 *     <li>{@code openId} — 必填，微信用户 OpenID</li>
 *     <li>{@code accountId} — 选填，公众号账号 ID（不传则使用默认账号）</li>
 *     <li>{@code templateId} — 选填，微信模板消息 ID（不传则发送纯文本客服消息）</li>
 * </ul>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(WeChatMessageService.class)
public class WeChatNotificationSender implements NotificationSender {

    private final WeChatMessageService weChatMessageService;

    @Override
    public NotificationType supportType() {
        return NotificationType.WECHAT;
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public boolean send(NotificationMessage message) {
        Map<String, Object> metadata = message.getMetadata();
        if (metadata == null || !metadata.containsKey("openId")) {
            log.warn("微信通知缺少 openId，跳过发送: receiver={}", message.getReceiver());
            return false;
        }

        String openId = (String) metadata.get("openId");
        Long accountId = metadata.containsKey("accountId")
                ? ((Number) metadata.get("accountId")).longValue() : null;
        String templateId = (String) metadata.get("templateId");

        try {
            if (templateId != null) {
                // 模板消息
                TemplateMessage templateMessage = TemplateMessage.builder()
                        .toUser(openId)
                        .templateId(templateId)
                        .build();

                // 将 templateParams 映射为模板数据
                if (message.getTemplateParams() != null) {
                    message.getTemplateParams().forEach((key, value) ->
                            templateMessage.addData(key, String.valueOf(value)));
                }

                weChatMessageService.sendTemplateMessage(accountId, templateMessage);
            } else {
                // 纯文本客服消息
                String content = message.getContent();
                if (message.getSubject() != null) {
                    content = message.getSubject() + "\n" + content;
                }
                weChatMessageService.sendTextMessage(accountId, openId, content);
            }

            log.info("微信通知发送成功: openId={}, accountId={}", openId, accountId);
            return true;

        } catch (WxErrorException e) {
            log.error("微信通知发送失败: openId={}, accountId={}", openId, accountId, e);
            return false;
        }
    }

    @Async
    @Override
    public void sendAsync(NotificationMessage message) {
        send(message);
    }

    // ==================== 便捷方法（保留原有业务语义）====================

    /**
     * 发送欢迎通知
     */
    public void sendWelcomeNotification(String openId, String username, String time) {
        sendWelcomeNotification(null, openId, username, time);
    }

    public void sendWelcomeNotification(Long accountId, String openId, String username, String time) {
        try {
            TemplateMessage message = TemplateMessage.builder()
                    .toUser(openId)
                    .templateId("welcome-template-id")
                    .build()
                    .addData("first", "欢迎注册！", "#173177")
                    .addData("keyword1", username)
                    .addData("keyword2", time)
                    .addData("remark", "感谢您的使用");

            weChatMessageService.sendTemplateMessage(accountId, message);
            log.info("欢迎通知发送成功，accountId: {}, openId: {}", accountId, openId);
        } catch (WxErrorException e) {
            log.error("发送欢迎通知失败，accountId: {}, openId: {}", accountId, openId, e);
        }
    }

    /**
     * 发送登录通知
     */
    public void sendLoginNotification(String openId, String ip, String location,
                                      String device, String time) {
        sendLoginNotification(null, openId, ip, location, device, time);
    }

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
     * 发送系统通知
     */
    public void sendSystemNotification(String openId, String title,
                                       String content, String time) {
        sendSystemNotification(null, openId, title, content, time);
    }

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
     * 发送简单文本消息
     */
    public void sendTextNotification(String openId, String content) {
        sendTextNotification(null, openId, content);
    }

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
     */
    public void sendBatchNotification(java.util.List<String> openIds, String content) {
        for (String openId : openIds) {
            sendTextNotification(openId, content);
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
