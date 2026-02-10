package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.system.handler.event.UserLoginEvent;
import com.rymcu.mortise.system.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created on 2025/7/22 9:26.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler
 */
@Slf4j
@Component
public class UserLoginEventHandler {

    @Resource
    private UserService userService;

    @Async
    @EventListener
    public void processUserLoginEvent(UserLoginEvent userLoginEvent) {
        userService.updateLastLoginTimeByAccount(userLoginEvent.getAccount());
    }

}
