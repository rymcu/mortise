package com.rymcu.mortise.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2025/2/27 14:32.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
@Component
public class RedisAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_AUTHORIZATION_REQUEST_PREFIX = "oauth2_auth_request:";
    private static final int AUTHORIZATION_REQUEST_EXPIRE_TIME = 10; // 过期时间(分钟)

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAuthorizationRequestRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        Assert.notNull(redisTemplate, "redisTemplate cannot be null");
        Assert.notNull(objectMapper, "objectMapper cannot be null");
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");

        String state = request.getParameter("state");
        if (state == null) {
            return null;
        }

        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value, OAuth2AuthorizationRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize authorization request", e);
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        String state = authorizationRequest.getState();
        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;

        try {
            String value = objectMapper.writeValueAsString(authorizationRequest);
            redisTemplate.opsForValue().set(key, value, AUTHORIZATION_REQUEST_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize authorization request", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");

        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            String state = authorizationRequest.getState();
            String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;
            redisTemplate.delete(key);
        }

        return authorizationRequest;
    }

    // 可选: 添加清理方法
    public void removeAuthorizationRequestByState(String state) {
        Assert.notNull(state, "state cannot be null");
        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;
        redisTemplate.delete(key);
    }
}
