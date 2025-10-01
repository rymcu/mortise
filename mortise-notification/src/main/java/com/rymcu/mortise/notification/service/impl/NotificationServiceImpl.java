package com.rymcu.mortise.notification.service.impl;

import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.service.NotificationService;
import com.rymcu.mortise.notification.spi.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务实现
 * 收集所有 NotificationSender 实现并根据类型分发
 *
 * @author ronger
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final List<NotificationSender> notificationSenders;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public NotificationServiceImpl(Optional<List<NotificationSender>> sendersOptional) {
        this.notificationSenders = sendersOptional.orElse(null);
    }

    /**
     * 通知类型 -> 发送器映射表
     */
    private final Map<NotificationType, NotificationSender> senderMap = new ConcurrentHashMap<>();

    /**
     * 初始化发送器映射表
     */
    private void initSenderMap() {
        if (senderMap.isEmpty() && notificationSenders != null && !notificationSenders.isEmpty()) {
            notificationSenders.stream()
                    .sorted(Comparator.comparingInt(NotificationSender::getOrder))
                    .forEach(sender -> {
                        NotificationType type = sender.supportType();
                        if (!senderMap.containsKey(type)) {
                            senderMap.put(type, sender);
                            log.info("注册通知发送器: {} -> {}", type, sender.getClass().getSimpleName());
                        }
                    });
        }
    }

    @Override
    public boolean send(NotificationMessage message) {
        initSenderMap();

        NotificationType type = message.getType();
        NotificationSender sender = senderMap.get(type);

        if (sender == null) {
            log.warn("没有找到 {} 类型的通知发送器", type);
            return false;
        }

        try {
            return sender.send(message);
        } catch (Exception e) {
            log.error("发送通知失败: type={}, receiver={}", type, message.getReceiver(), e);
            return false;
        }
    }

    @Async
    @Override
    public void sendAsync(NotificationMessage message) {
        initSenderMap();

        NotificationType type = message.getType();
        NotificationSender sender = senderMap.get(type);

        if (sender == null) {
            log.warn("没有找到 {} 类型的通知发送器", type);
            return;
        }

        try {
            sender.sendAsync(message);
        } catch (Exception e) {
            log.error("异步发送通知失败: type={}, receiver={}", type, message.getReceiver(), e);
        }
    }

    @Override
    public void sendBatch(List<NotificationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        initSenderMap();

        messages.forEach(message -> {
            try {
                send(message);
            } catch (Exception e) {
                log.error("批量发送通知失败: type={}, receiver={}", 
                        message.getType(), message.getReceiver(), e);
            }
        });
    }

    @Override
    public boolean send(NotificationType type, String receiver, String subject, String content) {
        NotificationMessage message = NotificationMessage.builder()
                .type(type)
                .receiver(receiver)
                .subject(subject)
                .content(content)
                .async(false)
                .build();

        return send(message);
    }
}
