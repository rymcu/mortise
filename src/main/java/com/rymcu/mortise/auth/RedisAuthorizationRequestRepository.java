package com.rymcu.mortise.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
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
    private static final Logger logger = LoggerFactory.getLogger(RedisAuthorizationRequestRepository.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 序列化策略选择: JACKSON, JAVA_SERIALIZATION, BASE64
    private static final String SERIALIZATION_STRATEGY = "BASE64";

    public RedisAuthorizationRequestRepository(StringRedisTemplate redisTemplate, ObjectMapper defaultObjectMapper) {
        Assert.notNull(redisTemplate, "redisTemplate cannot be null");
        Assert.notNull(defaultObjectMapper, "objectMapper cannot be null");
        this.redisTemplate = redisTemplate;

        // 配置专用于OAuth2序列化/反序列化的ObjectMapper
        this.objectMapper = configureOAuth2ObjectMapper();
    }

    /**
     * 配置适用于OAuth2对象的ObjectMapper
     */
    private ObjectMapper configureOAuth2ObjectMapper() {
        // 创建多态类型验证器，确保安全反序列化
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType(OAuth2AuthorizationRequest.class)
                .build();

        // 使用JsonMapper.builder可以提供更多配置选项
        ObjectMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                .build();

        // 注册Java 8日期时间模块
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 注册Spring Security的Jackson模块
        ClassLoader loader = getClass().getClassLoader();
        List<com.fasterxml.jackson.databind.Module> modules = SecurityJackson2Modules.getModules(loader);
        mapper.registerModules(modules);

        return mapper;
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

        return deserializeAuthorizationRequest(value);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        String state = authorizationRequest.getState();
        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;

        String serializedRequest = serializeAuthorizationRequest(authorizationRequest);
        redisTemplate.opsForValue().set(key, serializedRequest, AUTHORIZATION_REQUEST_EXPIRE_TIME, TimeUnit.MINUTES);
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

    /**
     * 序列化OAuth2AuthorizationRequest对象
     */
    private String serializeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            switch (SERIALIZATION_STRATEGY) {
                case "JACKSON":
                    return objectMapper.writeValueAsString(authorizationRequest);

                case "JAVA_SERIALIZATION":
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(authorizationRequest);
                    oos.close();
                    return Base64.getEncoder().encodeToString(baos.toByteArray());

                case "BASE64":
                default:
                    // 使用BASE64编码的Java序列化
                    baos = new ByteArrayOutputStream();
                    oos = new ObjectOutputStream(baos);
                    oos.writeObject(authorizationRequest);
                    oos.close();
                    return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            logger.error("Failed to serialize authorization request", e);
            throw new RuntimeException("Failed to serialize authorization request", e);
        }
    }

    /**
     * 反序列化OAuth2AuthorizationRequest对象
     */
    private OAuth2AuthorizationRequest deserializeAuthorizationRequest(String data) {
        try {
            switch (SERIALIZATION_STRATEGY) {
                case "JACKSON":
                    return objectMapper.readValue(data, OAuth2AuthorizationRequest.class);

                case "JAVA_SERIALIZATION":
                case "BASE64":
                default:
                    // 使用BASE64解码的Java反序列化
                    byte[] bytes = Base64.getDecoder().decode(data);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    Object obj = ois.readObject();
                    ois.close();
                    return (OAuth2AuthorizationRequest) obj;
            }
        } catch (Exception e) {
            logger.error("Failed to deserialize authorization request: {}", data, e);
            throw new RuntimeException("Failed to deserialize authorization request", e);
        }
    }
}
