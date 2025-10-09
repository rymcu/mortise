package com.rymcu.mortise.auth.repository;

import com.rymcu.mortise.auth.service.AuthCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 基于缓存的 OAuth2 授权请求存储
 * <p>
 * 将 OAuth2 授权请求存储在 Redis 中，而不是 Session 中
 * 这样可以支持无状态的 OAuth2 认证流程
 *
 * @author ronger
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String STATE_PARAMETER_NAME = "state";

    private final AuthCacheService authCacheService;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        String state = getStateParameter(request);
        if (!StringUtils.hasText(state)) {
            return null;
        }
        return getAuthorizationRequest(state);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        if (authorizationRequest == null) {
            String state = getStateParameter(request);
            if (StringUtils.hasText(state)) {
                removeAuthorizationRequest(state);
            }
            return;
        }

        String state = authorizationRequest.getState();
        Assert.hasText(state, "authorizationRequest.state cannot be empty");

        authCacheService.storeOAuth2AuthorizationRequest(state, authorizationRequest);

        // 缓存
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            authCacheService.storeOAuth2ParameterMap(state, parameterMap);
        }

        log.debug("保存 OAuth2 授权请求: state={}", state);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        String state = getStateParameter(request);
        if (!StringUtils.hasText(state)) {
            return null;
        }

        OAuth2AuthorizationRequest authorizationRequest = getAuthorizationRequest(state);
        if (authorizationRequest != null) {
            removeAuthorizationRequest(state);
        }
        return authorizationRequest;
    }

    /**
     * 从缓存中获取授权请求
     */
    private OAuth2AuthorizationRequest getAuthorizationRequest(String state) {
        OAuth2AuthorizationRequest request = authCacheService.getOAuth2AuthorizationRequest(
                state, OAuth2AuthorizationRequest.class);
        log.debug("从缓存加载 OAuth2 授权请求: state={} -> {}", state, request != null ? "存在" : "不存在");
        return request;
    }

    /**
     * 从缓存中删除授权请求
     */
    private void removeAuthorizationRequest(String state) {
        authCacheService.removeOAuth2AuthorizationRequest(state);
        log.debug("移除 OAuth2 授权请求: state={}", state);
    }

    /**
     * 从请求中获取 state 参数
     */
    private String getStateParameter(HttpServletRequest request) {
        return request.getParameter(STATE_PARAMETER_NAME);
    }
}
