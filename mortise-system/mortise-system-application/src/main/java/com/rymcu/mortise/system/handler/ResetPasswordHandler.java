package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.system.handler.event.ResetPasswordEvent;
import com.rymcu.mortise.system.service.SystemNotificationService;
import jakarta.annotation.Resource;
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
    private SystemNotificationService systemNotificationService;

    @TransactionalEventListener
    public void processResetPasswordEvent(ResetPasswordEvent resetPasswordEvent) {
        systemNotificationService.sendPasswordResetEmail(resetPasswordEvent.getEmail(), resetPasswordEvent.getCode());
    }

}
