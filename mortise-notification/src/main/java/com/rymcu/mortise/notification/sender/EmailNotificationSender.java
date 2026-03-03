package com.rymcu.mortise.notification.sender;

import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.spi.NotificationChannelConfigProvider;
import com.rymcu.mortise.notification.spi.NotificationSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * 邮件通知发送器实现
 * <p>
 * 配置来源已从静态 yml 文件改为由 {@link NotificationChannelConfigProvider} 提供，
 * 支持通过管理端界面动态修改邮件配置（SMTP 地址、端口、账号等）。
 * 每次发送前从 Provider 获取最新配置（Provider 内部有缓存，管理端修改后自动刷新）。
 *
 * @author ronger
 */
@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    private final NotificationChannelConfigProvider configProvider;
    private final TemplateEngine templateEngine;

    /**
     * 构造函数注入
     * <p>
     * {@code TemplateEngine} 为可选依赖，未引入 Thymeleaf 时不影响基础邮件发送功能。
     */
    @Autowired
    public EmailNotificationSender(
            NotificationChannelConfigProvider configProvider,
            Optional<TemplateEngine> templateEngineOptional) {
        this.configProvider = configProvider;
        this.templateEngine = templateEngineOptional.orElse(null);
    }

    @Override
    public NotificationType supportType() {
        return NotificationType.EMAIL;
    }

    @Override
    public boolean send(NotificationMessage message) {
        // 运行时检查渠道是否已配置并启用
        if (!configProvider.isEnabled(NotificationType.EMAIL)) {
            log.warn("邮件渠道未启用或尚未配置，跳过发送: receiver={}", message.getReceiver());
            return false;
        }

        Map<String, String> config = configProvider.getConfig(NotificationType.EMAIL);
        JavaMailSenderImpl mailSender = buildMailSender(config);
        String fromEmail = config.getOrDefault("username", "");
        String fromName = config.getOrDefault("from_name", Utils.formatEmailDomain(fromEmail));

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(message.getReceiver());
            helper.setSubject(message.getSubject());

            // 如果有模板，使用模板引擎渲染
            String content = message.getContent();
            if (message.getTemplate() != null && templateEngine != null) {
                Context context = new Context();
                if (message.getTemplateParams() != null) {
                    context.setVariables(message.getTemplateParams());
                }
                content = templateEngine.process(message.getTemplate(), context);
            }

            helper.setText(content, true);
            mailSender.send(mimeMessage);

            log.info("邮件发送成功: receiver={}, subject={}", message.getReceiver(), message.getSubject());
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("邮件发送失败: receiver={}, subject={}",
                    message.getReceiver(), message.getSubject(), e);
            return false;
        }
    }

    @Async
    @Override
    public void sendAsync(NotificationMessage message) {
        send(message);
    }

    /**
     * 根据数据库配置动态构建 {@link JavaMailSenderImpl} 实例
     * <p>
     * 每次调用均构建新实例，保证配置变更后立即生效（不需要重启服务）。
     *
     * @param config 渠道配置 Map
     * @return 配置好的 JavaMailSenderImpl
     */
    private JavaMailSenderImpl buildMailSender(Map<String, String> config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getOrDefault("host", ""));
        sender.setPort(Integer.parseInt(config.getOrDefault("port", "465")));
        sender.setUsername(config.getOrDefault("username", ""));
        sender.setPassword(config.getOrDefault("password", ""));
        sender.setDefaultEncoding("UTF-8");

        boolean ssl = Boolean.parseBoolean(config.getOrDefault("ssl", "true"));
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", ssl);
        if (ssl) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        return sender;
    }
}
