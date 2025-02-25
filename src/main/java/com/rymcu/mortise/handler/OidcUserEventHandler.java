package com.rymcu.mortise.handler;

import com.alibaba.fastjson.JSONObject;
import com.rymcu.mortise.handler.event.OidcUserEvent;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * Created on 2025/2/25 11:02.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler
 */
@Slf4j
@Component
public class OidcUserEventHandler {

    @Resource
    private UserService userService;

    @Async
    @EventListener
    public void processAccountLastOnlineTimeEvent(OidcUserEvent oidcUserEvent) {
        OidcUser user = oidcUserEvent.getUser();
        System.out.println(JSONObject.toJSONString(user));
    }

}
