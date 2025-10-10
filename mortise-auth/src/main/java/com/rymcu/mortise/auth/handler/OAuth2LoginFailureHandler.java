package com.rymcu.mortise.auth.handler;

import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.service.AuthCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created on 2025/10/10 23:41.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.handler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    private final AuthCacheService authCacheService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("===============================================");
        log.error("!!! OAuth2 登录失败 !!!");
        log.error("Request URI: {}", request.getRequestURI());
        log.error("异常类型: {}", exception.getClass().getName());
        log.error("异常消息: {}", exception.getMessage());
        log.error("===============================================", exception);

        String state = request.getParameter("state");

        // 更新为取消状态
        if (StringUtils.isNotBlank(state)) {
            authCacheService.storeOAuth2QrcodeState(
                    state,
                    QrcodeState.CANCELED.getValue()
            );
            log.info("用户取消授权，state: {}", state);
        }

        // 返回错误信息
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(String.format(
                "{\"success\":false,\"message\":\"OAuth2 登录失败: %s\"}",
                exception.getMessage()
        ));
    }
}
