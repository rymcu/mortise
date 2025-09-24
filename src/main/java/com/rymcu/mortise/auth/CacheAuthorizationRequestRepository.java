package com.rymcu.mortise.auth;

import com.rymcu.mortise.service.CacheService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 通过统一缓存服务存储OAuth2授权请求的实现类
 * 优化版本：使用 CacheService 统一管理缓存操作
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
@Slf4j
@Component("cacheAuthorizationRequestRepository")
public class CacheAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    @Resource
    private CacheService cacheService;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");

        String state = request.getParameter("state");
        if (state == null) {
            log.debug("请求中没有 state 参数");
            return null;
        }

        // 使用统一缓存服务获取授权请求
        OAuth2AuthorizationRequest authorizationRequest = cacheService.getOAuth2AuthorizationRequest(
                state, OAuth2AuthorizationRequest.class);

        if (authorizationRequest != null) {
            log.debug("成功从缓存加载 OAuth2 授权请求，state: {}", state);
        } else {
            log.debug("缓存中未找到 OAuth2 授权请求，state: {}", state);
        }

        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        String state = authorizationRequest.getState();

        // 使用统一缓存服务存储授权请求
        cacheService.storeOAuth2AuthorizationRequest(state, authorizationRequest);
        log.debug("成功保存 OAuth2 授权请求到缓存，state: {}", state);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");

        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            String state = authorizationRequest.getState();
            // 使用统一缓存服务删除授权请求
            cacheService.removeOAuth2AuthorizationRequest(state);
            log.debug("成功从缓存删除 OAuth2 授权请求，state: {}", state);
        }

        return authorizationRequest;
    }

    /**
     * 根据state直接删除授权请求
     *
     * @param state 授权状态参数
     */
    public void removeAuthorizationRequestByState(String state) {
        Assert.notNull(state, "state cannot be null");
        cacheService.removeOAuth2AuthorizationRequest(state);
        log.debug("根据 state 删除 OAuth2 授权请求: {}", state);
    }

    /**
     * 检查指定state的授权请求是否存在
     *
     * @param state 授权状态参数
     * @return 是否存在
     */
    public boolean existsAuthorizationRequest(String state) {
        Assert.notNull(state, "state cannot be null");
        OAuth2AuthorizationRequest request = cacheService.getOAuth2AuthorizationRequest(
                state, OAuth2AuthorizationRequest.class);
        boolean exists = request != null;
        log.debug("检查 OAuth2 授权请求是否存在，state: {}, exists: {}", state, exists);
        return exists;
    }
}
