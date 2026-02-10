package com.rymcu.mortise.member.api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.spi.OAuth2UserInfoExtractor;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import com.rymcu.mortise.auth.util.OAuth2ProviderUtils;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.member.api.model.OAuth2LoginResponse;
import com.rymcu.mortise.member.api.service.OAuth2MemberBindingService;
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
 * API 客户端 OAuth2 登录成功处理器
 * <p>
 * 处理客户端（APP）的 OAuth2 登录（微信、GitHub、Google 等）
 * <p>
 * 支持的 registrationId:
 * - wechat-app: 微信 APP 登录
 * - github-app: GitHub APP 登录
 * - google-app: Google APP 登录
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserInfoExtractor userInfoExtractor;

    private final OAuth2MemberBindingService oauth2MemberBindingService;

    private final ObjectMapper objectMapper;

    private final AuthCacheService authCacheService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("API 客户端 OAuth2 登录成功处理器被调用");

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

            log.info("API 客户端 OAuth2 登录: registrationId={}, provider={}, openId={}, email={}",
                    registrationId, userInfo.getProvider(), userInfo.getOpenId(), userInfo.getEmail());

            // 2. 确定提供商类型
            String providerType = OAuth2ProviderUtils.determineProviderType(registrationId);

            // 3. 绑定或登录会员
            OAuth2LoginResponse loginResponse = oauth2MemberBindingService.bindOrLoginWithOAuth2User(
                    oauth2User, providerType, registrationId);

            // 4. 获取传递的 parameterMap
            MultiValueMap<String, String> parameterMap;
            String state = request.getParameter("state");
            if (StringUtils.isEmpty(state)) {
                parameterMap = new LinkedMultiValueMap<>();
            } else {
                // 更新二维码状态为已授权
                if (StringUtils.isNotBlank(state)) {
                    authCacheService.storeOAuth2QrcodeState(
                            state,
                            QrcodeState.AUTHORIZED.getValue()
                    );
                    log.info("用户授权成功，state: {}", state);
                }
                parameterMap = authCacheService.getOAuth2ParameterMap(state, MultiValueMap.class);
            }

            state = Utils.genKey();

            // 5. 根据 registrationId 决定响应方式
            handleSuccessResponse(response, loginResponse, userInfo.getRedirectUri(), state, parameterMap);

            log.info("API 客户端 OAuth2 登录成功: memberId={}, username={}",
                    loginResponse.memberId(), loginResponse.username());

        } catch (Exception e) {
            log.error("API 客户端 OAuth2 登录失败: registrationId={}", registrationId, e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    /**
     * 根据 registrationId 处理成功响应
     */
    private void handleSuccessResponse(HttpServletResponse response,
                                       OAuth2LoginResponse loginResponse,
                                       String redirectUrl,
                                       String state,
                                       MultiValueMap<String, String> parameterMap) throws IOException {
        if (StringUtils.isEmpty(redirectUrl)) {
            // 默认：返回 JSON
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    GlobalResult.success(loginResponse)
            ));
        } else {
            // 与 SystemOAuth2LoginSuccessHandler 保持一致：
            // 将 token 存储在服务端，只传递 state 给前端，前端通过 state 兑换 token
            authCacheService.storeOAuth2LoginResponse(state, loginResponse);
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
