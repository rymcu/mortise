package com.rymcu.mortise.auth.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 统一的、可分发的 OAuth2UserService。
 * <p>
 * 这个 Service 是处理所有 OAuth2/OIDC 登录的核心。
 * 它能够根据 registrationId 自动分发到正确的处理器：
 * <ul>
 *   <li>OIDC 提供商 (e.g., Google) -> 委托给 OidcUserService</li>
 *   <li>自定义提供商 (e.g., WeChat) -> 使用自定义逻辑</li>
 *   <li>标准 OAuth2 提供商 (e.g., GitHub) -> 委托给 DefaultOAuth2UserService</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final RestTemplate restTemplate;
    // 持有 Spring 默认的两个 Service 作为委托目标
    private final OidcUserService oidcUserService = new OidcUserService();
    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.debug("Loading user for registrationId: {}", registrationId);

        // --- 1. 判断是否是 OIDC 登录 (例如 Google) ---
        // Spring Security 会自动将 OidcUserRequest 传递给 OIDC 流程
        if (userRequest instanceof OidcUserRequest) {
            log.info("Handling OIDC user request for {}", registrationId);
            return oidcUserService.loadUser((OidcUserRequest) userRequest);
        }

        // --- 2. 判断是否是自定义的微信登录 ---
        // 通常可以根据 registrationId 的前缀或特定名称判断
        if (registrationId.toLowerCase().contains("wechat")) {
            log.info("Handling custom WeChat user request for {}", registrationId);
            return loadWeChatUser(userRequest);
        }

        // --- 3. 其他所有情况，视为标准 OAuth2 登录 (例如 GitHub) ---
        log.info("Handling standard OAuth2 user request for {}", registrationId);
        return defaultOAuth2UserService.loadUser(userRequest);
    }

    private OAuth2User loadWeChatUser(OAuth2UserRequest userRequest) {
        // 这里的逻辑与我们之前实现的 CustomOAuth2UserService 中的完全一样
        String accessToken = userRequest.getAccessToken().getTokenValue();
        Map<String, Object> additionalParameters = userRequest.getAdditionalParameters();
        String openid = (String) additionalParameters.get("openid");

        String userInfoUri = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
        String url = userInfoUri + "?access_token=" + accessToken + "&openid=" + openid + "&lang=zh_CN";

        @SuppressWarnings("unchecked")
        Map<String, Object> userAttributes = restTemplate.getForObject(url, Map.class);

        if (userAttributes == null || userAttributes.containsKey("errcode")) {
            String errMsg = userAttributes != null ? userAttributes.get("errmsg").toString() : "Failed to fetch user info from WeChat";
            throw new OAuth2AuthenticationException(errMsg);
        }

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // ... 构建 DefaultOAuth2User 并返回
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                userAttributes,
                userNameAttributeName
        );
    }
}
