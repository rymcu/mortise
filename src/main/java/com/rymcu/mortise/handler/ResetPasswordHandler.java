package com.rymcu.mortise.handler;

import com.rymcu.mortise.handler.event.ResetPasswordEvent;
import com.rymcu.mortise.service.JavaMailService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Created on 2024/4/18 8:10.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler
 */
@Slf4j
@Component
public class ResetPasswordHandler {
    @Resource
    private JavaMailService javaMailService;

    @TransactionalEventListener
    public void processResetPasswordEvent(ResetPasswordEvent resetPasswordEvent) {
        try {
            javaMailService.sendInitialPassword(resetPasswordEvent.getEmail(), resetPasswordEvent.getCode());
        } catch (MessagingException e) {
            log.error("发送用户初始密码邮件失败！", e);
        }
    }

}
