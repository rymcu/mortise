package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.service.NotificationService;
import com.rymcu.mortise.system.service.SystemNotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统通知服务实现
 * 封装基础设施层的 NotificationService，提供业务语义化操作
 *
 * @author ronger
 */
@Slf4j
@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    @Resource
    private NotificationService notificationService;

    @Override
    public boolean sendWelcomeEmail(String email, String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);

        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject("欢迎加入 RYMCU 社区")
                .template("welcome-email")
                .templateParams(params)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        log.info("发送欢迎邮件: email={}, success={}", email, success);
        return success;
    }

    @Override
    public boolean sendVerificationCodeEmail(String email, String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);

        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject("验证码")
                .template("verification-code-email")
                .templateParams(params)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        log.info("发送验证码邮件: email={}, success={}", email, success);
        return success;
    }

    @Override
    public boolean sendPasswordResetEmail(String email, String resetToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("resetToken", resetToken);
        params.put("resetUrl", "https://rymcu.com/reset-password?token=" + resetToken);

        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject("密码重置")
                .template("password-reset-email")
                .templateParams(params)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        log.info("发送密码重置邮件: email={}, success={}", email, success);
        return success;
    }

    @Override
    public boolean sendAccountActivationEmail(String email, String activationToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("activationToken", activationToken);
        params.put("activationUrl", "https://rymcu.com/activate?token=" + activationToken);

        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject("账号激活")
                .template("account-activation-email")
                .templateParams(params)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        log.info("发送账号激活邮件: email={}, success={}", email, success);
        return success;
    }

    @Override
    public boolean sendSystemNotificationEmail(String email, String subject, String content) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject(subject)
                .content(content)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        log.info("发送系统通知邮件: email={}, subject={}, success={}", email, subject, success);
        return success;
    }

    @Override
    public boolean sendTemplateEmail(String email, String subject, String templateName, Map<String, Object> templateParams) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject(subject)
                .template(templateName)
                .templateParams(templateParams)
                .async(false)
                .build();

        return notificationService.send(message);
    }

    @Override
    public void sendEmailAsync(String email, String subject, String content) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.EMAIL)
                .receiver(email)
                .subject(subject)
                .content(content)
                .async(true)
                .build();

        notificationService.sendAsync(message);
        log.debug("异步发送邮件: email={}, subject={}", email, subject);
    }

    @Override
    public boolean sendSystemMessage(Long userId, String title, String content) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.SYSTEM)
                .receiver(String.valueOf(userId))
                .subject(title)
                .content(content)
                .async(false)
                .build();

        boolean success = notificationService.send(message);
        log.info("发送站内消息: userId={}, title={}, success={}", userId, title, success);
        return success;
    }
}
