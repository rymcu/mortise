package com.rymcu.mortise.notification.spi;

import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;

/**
 * 通知发送器 SPI 接口
 * 业务模块可实现此接口来扩展不同的通知发送方式
 * 
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class EmailNotificationSender implements NotificationSender {
 *     &#64;Override
 *     public NotificationType supportType() {
 *         return NotificationType.EMAIL;
 *     }
 *     
 *     &#64;Override
 *     public void send(NotificationMessage message) {
 *         // 发送邮件逻辑
 *     }
 * }
 * </pre>
 *
 * @author ronger
 */
public interface NotificationSender {

    /**
     * 获取优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 支持的通知类型
     */
    NotificationType supportType();

    /**
     * 发送通知
     *
     * @param message 通知消息
     * @return 是否发送成功
     */
    boolean send(NotificationMessage message);

    /**
     * 异步发送通知
     * 默认实现为调用同步发送方法
     *
     * @param message 通知消息
     */
    default void sendAsync(NotificationMessage message) {
        send(message);
    }

    /**
     * 批量发送通知
     * 默认实现为循环调用 send
     *
     * @param messages 通知消息列表
     */
    default void sendBatch(java.util.List<NotificationMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            messages.forEach(this::send);
        }
    }
}
