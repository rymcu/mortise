package com.rymcu.mortise.system.handler;

import com.rymcu.mortise.auth.service.OAuth2UserInfoExtractor;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.auth.TokenUser;
import com.rymcu.mortise.system.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * 系统管理员 OAuth2 登录成功处理器
 * <p>
 * 处理管理后台的 OAuth2 登录（Logto、GitHub、Google 等）
 * <p>
 * 简化版本：直接使用 StandardOAuth2UserInfo，无需额外上下文
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component("systemOAuth2LoginSuccessHandler")
public class SystemOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private OAuth2UserInfoExtractor userInfoExtractor;

    @Resource
    private AuthService authService;

    @Resource
    private TokenManager tokenManager;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("系统管理员 OAuth2 登录成功处理器被调用");

        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Auth)) {
            log.warn("认证类型不正确: {}", authentication.getClass());
            sendErrorResponse(response, "认证类型不正确");
            return;
        }

        OAuth2User oauth2User = oauth2Auth.getPrincipal();
        String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();

        try {
            // 1. 提取标准化的用户信息（包含 registrationId）
            StandardOAuth2UserInfo userInfo = userInfoExtractor.extractUserInfo(oauth2User, registrationId);

            log.info("系统管理员 OAuth2 登录: registrationId={}, provider={}, openId={}, email={}",
                registrationId, userInfo.getProvider(), userInfo.getOpenId(), userInfo.getEmail());

            // 2. 查找或创建系统用户（简化：无需上下文参数）
            User user = authService.findOrCreateUserFromOAuth2(userInfo);

            // 3. 生成 JWT Token
            TokenUser tokenUser = authService.generateTokens(user);

            // 4. 返回 JSON 响应
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                GlobalResult.success(tokenUser)
            ));

            log.info("系统管理员 OAuth2 登录成功: userId={}, account={}",
                user.getId(), user.getAccount());

        } catch (Exception e) {
            log.error("系统管理员 OAuth2 登录失败: registrationId={}", registrationId, e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(objectMapper.writeValueAsString(
            GlobalResult.error(message)
        ));
    }
}
