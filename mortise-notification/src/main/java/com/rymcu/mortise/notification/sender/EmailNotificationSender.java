package com.rymcu.mortise.notification.sender;

import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.notification.entity.NotificationMessage;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.spi.NotificationSender;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * 邮件通知发送器实现
 *
 * @author ronger
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class EmailNotificationSender implements NotificationSender {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final TemplateEngine templateEngine;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public EmailNotificationSender(Optional<TemplateEngine> templateEngineOptional) {
        this.templateEngine = templateEngineOptional.orElse(null);
    }

    @Override
    public NotificationType supportType() {
        return NotificationType.EMAIL;
    }

    @Override
    public boolean send(NotificationMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, Utils.formatEmailDomain(fromEmail));

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

            helper.setText(content, true); // true表示HTML格式

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
}
