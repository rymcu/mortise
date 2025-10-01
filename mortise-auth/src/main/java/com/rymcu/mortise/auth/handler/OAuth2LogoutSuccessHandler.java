package com.rymcu.mortise.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

/**
 * OAuth2 登出成功处理器
 * <p>
 * 处理 OAuth2 登出成功后的逻辑
 *
 * @author ronger
 */
@Slf4j
public class OAuth2LogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Authentication authentication)
            throws IOException, ServletException {
        log.info("OAuth2 登出成功");
        
        // 可以在这里添加登出后的处理逻辑
        // 例如：清除缓存、记录日志等
    }
}
