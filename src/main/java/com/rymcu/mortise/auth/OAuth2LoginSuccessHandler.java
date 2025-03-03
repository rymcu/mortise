package com.rymcu.mortise.auth;

import com.rymcu.mortise.model.TokenUser;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Created on 2025/2/25 9:36.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("OAuth2 Login Success");
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();
            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                TokenUser tokenUser = userService.oauth2Login(oidcUser, registrationId);
                response.sendRedirect("/auth/callback?token=" + tokenUser.getToken() + "&refreshToken=" + tokenUser.getRefreshToken());
            }
        }
    }
}
