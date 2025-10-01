package com.rymcu.mortise.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * OAuth2 登录成功处理器
 * <p>
 * 处理 OAuth2/OIDC 登录成功后的逻辑
 * 具体的业务逻辑（如生成 JWT Token）由 system 模块的 AuthService.oauth2Login 实现
 *
 * @author ronger
 */
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("OAuth2 登录成功处理器被调用");
        
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();
            
            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                log.info("OAuth2 登录成功: registrationId={}, email={}", 
                        registrationId, oidcUser.getEmail());
                
                // OAuth2 登录成功后，由 system 模块的 AuthController 处理具体业务逻辑
                // 这里只是简单重定向到回调页面
                // 实际的 Token 生成在 AuthController 的 oauth2Callback 方法中完成
                String redirectUrl = "/api/v1/auth/oauth2/callback?registrationId=" + registrationId;
                
                log.info("重定向到: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
            }
        }
    }
}
