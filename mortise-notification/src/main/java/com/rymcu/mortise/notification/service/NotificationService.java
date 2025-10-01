package com.rymcu.mortise.notification.service;

import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;

import java.util.List;

/**
 * 通知服务接口
 * 提供统一的通知发送能力（基础设施层）
 * 
 * 注意：业务模块不应该直接使用此接口
 * 应该在业务模块中封装业务通知服务（如 SystemNotificationService）
 *
 * @author ronger
 */
public interface NotificationService {

    /**
     * 发送通知
     *
     * @param message 通知消息
     * @return 是否发送成功
     */
    boolean send(NotificationMessage message);

    /**
     * 异步发送通知
     *
     * @param message 通知消息
     */
    void sendAsync(NotificationMessage message);

    /**
     * 批量发送通知
     *
     * @param messages 通知消息列表
     */
    void sendBatch(List<NotificationMessage> messages);

    /**
     * 发送指定类型的通知
     *
     * @param type 通知类型
     * @param receiver 接收者
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    boolean send(NotificationType type, String receiver, String subject, String content);
}
