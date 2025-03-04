package com.rymcu.mortise.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Resource
    private ObjectMapper objectMapper;

    @Async
    @EventListener
    public void processAccountLastOnlineTimeEvent(OidcUserEvent oidcUserEvent) throws JsonProcessingException {
        OidcUser user = oidcUserEvent.getUser();
        System.out.println(objectMapper.writeValueAsString(user));
    }

}
