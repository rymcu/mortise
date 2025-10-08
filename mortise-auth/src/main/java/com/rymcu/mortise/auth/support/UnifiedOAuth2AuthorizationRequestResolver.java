package com.rymcu.mortise.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 统一的 OAuth2 授权请求解析器
 * <p>
 * 集成所有平台的特殊处理逻辑（如微信 #wechat_redirect），避免重复实现。
 * <ul>
 *   <li>微信相关的 registrationId -> 添加 #wechat_redirect 锚点</li>
 *   <li>其他提供商 -> 使用标准的授权请求处理</li>
 * </ul>
 * <p>
 * 优势：
 * <ul>
 *   <li>非侵入性：不需要修改 WebSecurityConfig</li>
 *   <li>扩展性：可以轻松为其他提供商添加专用处理逻辑</li>
 *   <li>向后兼容：对于不需要特殊处理的提供商，使用默认逻辑</li>
 * </ul>
 */
@Slf4j
@Component
public class UnifiedOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public UnifiedOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
        log.info("初始化统一 OAuth2 授权请求解析器");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = this.defaultResolver.resolve(request);
        return customizeAuthorizationRequest(request, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = this.defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(request, authorizationRequest);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        OAuth2AuthorizationRequest.Builder requestBuilder = OAuth2AuthorizationRequest.from(authorizationRequest);
        String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        // --- 针对微信的特殊处理逻辑 ---
        if (isWeChatProvider(registrationId)) {
            // 1. 添加 #wechat_redirect 锚点 (如果需要)
            if (hasWebChatAuthScope(authorizationRequest.getScopes())) {
                log.debug("为微信登录添加 #wechat_redirect 锚点: {}", registrationId);
                requestBuilder.authorizationRequestUri(uri -> uri.fragment("wechat_redirect").build());
            }

            // 2. 添加微信专用的 'appid' 参数
            String clientId = authorizationRequest.getClientId();
            requestBuilder.additionalParameters(params -> params.put("appid", clientId));
            log.debug("为微信登录添加 'appid' 参数: {}", clientId);
        }
        // --- 判断 referer 是否在白名单 ---
        String referer = request.getHeader("referer");
        if (referer != null && !referer.isBlank()) {
            // TODO 判断 referer 是否在白名单
            log.info("referer: {}", referer);
        }
        // 未来可扩展：如钉钉、企业微信等
        return requestBuilder.build();
    }

    private boolean isWeChatProvider(String registrationId) {
        if (registrationId == null) return false;
        String lower = registrationId.toLowerCase();
        return lower.contains("wechat") || lower.contains("weixin") || lower.startsWith("wx");
    }

    /**
     * 检查 scope 是否包含 'snsapi_base' 或者 'snsapi_userinfo'
     */
    private boolean hasWebChatAuthScope(Set<String> scopes) {
        return scopes != null && (scopes.contains("snsapi_base") || scopes.contains("snsapi_userinfo"));
    }
}
