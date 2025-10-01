package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.system.handler.event.AccountEvent;
import com.rymcu.mortise.system.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created on 2022/8/24 14:44.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @packageName com.rymcu.mortise.handler
 */
@Slf4j
@Component
public class AccountHandler {

    @Resource
    private UserService userService;

    @Async
    @EventListener
    public void processAccountLastOnlineTimeEvent(AccountEvent accountEvent) {
        userService.updateLastOnlineTimeByAccount(accountEvent.getAccount());
    }

}
