package com.rymcu.mortise.system.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.spi.OAuth2UserInfoExtractor;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.auth.TokenUser;
import com.rymcu.mortise.system.service.AuthService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;

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
@Component
@RequiredArgsConstructor
public class SystemOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserInfoExtractor userInfoExtractor;

    private final AuthService authService;

    private final ObjectMapper objectMapper;

    private final AuthCacheService authCacheService;

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
            // 4. 获取传递的 parameterMap
            MultiValueMap<String, String> parameterMap;
            String state = request.getParameter("state");
            if (StringUtils.isEmpty(state)) {
                parameterMap = new LinkedMultiValueMap<>();
            } else {
                parameterMap = authCacheService.getOAuth2ParameterMap(state, MultiValueMap.class);
            }

            state = Utils.genKey();

            // 5. 根据 registrationId 决定响应方式
            handleSuccessResponse(response, registrationId, tokenUser, userInfo.getRedirectUri(), state, parameterMap);

            log.info("系统管理员 OAuth2 登录成功: userId={}, account={}",
                    user.getId(), user.getAccount());

        } catch (Exception e) {
            log.error("系统管理员 OAuth2 登录失败: registrationId={}", registrationId, e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    /**
     * 根据 registrationId 处理成功响应
     */
    private void handleSuccessResponse(HttpServletResponse response,
                                       String registrationId,
                                       TokenUser tokenUser, String redirectUrl, String state, MultiValueMap<String, String> parameterMap) throws IOException {
        if (StringUtils.isEmpty(redirectUrl)) {
            // 默认：返回 JSON
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    GlobalResult.success(tokenUser)
            ));
        } else {
            authService.storeOauth2TokenUser(state, tokenUser);
            if (Objects.isNull(parameterMap)) {
                parameterMap = new LinkedMultiValueMap<>();
            }
            redirectUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("state", state)
                    .queryParams(parameterMap)
                    .build(true).toUriString();
            log.info("重定向到: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
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
