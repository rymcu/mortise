package com.rymcu.mortise.auth.support;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 统一的 OAuth2 AccessToken 响应客户端
 * <p>
 * 类似于 UnifiedOAuth2UserService 的设计理念，这个组件提供统一的入口，
 * 根据不同的 OAuth2 提供商自动选择合适的处理器：
 * <ul>
 *   <li>微信相关的 registrationId -> 使用微信专用处理逻辑</li>
 *   <li>其他提供商 -> 使用标准 OAuth2 处理逻辑</li>
 * </ul>
 * <p>
 * 这种设计的优势：
 * <ul>
 *   <li>非侵入性：不需要修改 WebSecurityConfig</li>
 *   <li>扩展性：可以轻松添加其他提供商的专用处理逻辑</li>
 *   <li>向后兼容：对于不需要特殊处理的提供商，使用默认逻辑</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedOAuth2AccessTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WeChatAccessTokenResponseClient weChatClient;

    // 非 final 字段，在构造后初始化
    private RestOperations restOperations;
    private final DefaultMapOAuth2AccessTokenResponseConverter responseConverter = new DefaultMapOAuth2AccessTokenResponseConverter();

    /**
     * 使用 @PostConstruct 注解，在依赖注入完成后执行初始化逻辑
     */
    @PostConstruct
    public void init() {
        this.restOperations = createStandardRestTemplate();
        log.info("初始化统一 OAuth2 AccessToken 响应客户端，并注入了微信专用客户端");
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(
            OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {

        String registrationId = authorizationCodeGrantRequest
                .getClientRegistration()
                .getRegistrationId();

        log.debug("处理 OAuth2 Token 请求: registrationId={}", registrationId);

        // 根据 registrationId 判断使用哪个客户端
        if (isWeChatProvider(registrationId)) {
            log.info("使用微信专用 Token 客户端处理: {}", registrationId);
            return weChatClient.getTokenResponse(authorizationCodeGrantRequest);
        }

        // 其他情况使用标准处理逻辑
        log.debug("使用标准 Token 客户端处理: {}", registrationId);
        return getStandardTokenResponse(authorizationCodeGrantRequest);
    }

    /**
     * 标准的 OAuth2 Token 获取逻辑
     *
     * @param grantRequest 授权请求
     * @return Token 响应
     */
    private OAuth2AccessTokenResponse getStandardTokenResponse(
            OAuth2AuthorizationCodeGrantRequest grantRequest) {

        try {
            // 构建标准的 Token 请求
            RequestEntity<?> request = buildStandardTokenRequest(grantRequest);

            // 发送请求

            Map<String, Object> responseMap = exchangeResponseEntity(request, this.restOperations, RESPONSE_TYPE);

            // 转换为标准的 OAuth2AccessTokenResponse

            return this.responseConverter.convert(responseMap);

        } catch (RestClientException ex) {
            log.error("获取访问令牌时发生网络错误", ex);
            OAuth2Error oauth2Error = new OAuth2Error(
                "invalid_token_response",
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
                        + ex.getMessage(),
                null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
    }

    static Map<String, Object> exchangeResponseEntity(RequestEntity<?> request, RestOperations restOperations, ParameterizedTypeReference<Map<String, Object>> responseType) {
        ResponseEntity<Map<String, Object>> responseEntity =
            restOperations.exchange(request, responseType);

        Map<String, Object> responseMap = responseEntity.getBody();

        if (responseMap == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token_response", "Empty response body", null)
            );
        }
        return responseMap;
    }

    /**
     * 构建标准的 OAuth2 Token 请求
     */
    private RequestEntity<?> buildStandardTokenRequest(OAuth2AuthorizationCodeGrantRequest grantRequest) {
        ClientRegistration clientRegistration = grantRequest.getClientRegistration();
        OAuth2AuthorizationExchange authorizationExchange = grantRequest.getAuthorizationExchange();

        // 构建请求参数
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add(OAuth2ParameterNames.GRANT_TYPE, grantRequest.getGrantType().getValue());
        parameters.add(OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());

        String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();
        if (redirectUri != null) {
            parameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
        }

        parameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
        if (clientRegistration.getClientSecret() != null) {
            parameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 获取 Token URI
        URI tokenUri = UriComponentsBuilder
            .fromUriString(clientRegistration.getProviderDetails().getTokenUri())
            .build()
            .toUri();

        return new RequestEntity<>(parameters, headers, HttpMethod.POST, tokenUri);
    }

    /**
     * 创建标准的 RestTemplate
     */
    private RestTemplate createStandardRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 配置消息转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        FormHttpMessageConverter formConverter = new FormHttpMessageConverter();

        restTemplate.setMessageConverters(Arrays.asList(formConverter, jsonConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        return restTemplate;
    }

    /**
     * 判断是否是微信相关的提供商
     *
     * @param registrationId 客户端注册ID
     * @return 是否是微信提供商
     */
    private boolean isWeChatProvider(String registrationId) {
        if (registrationId == null) {
            return false;
        }

        String lowerCaseId = registrationId.toLowerCase();

        // 支持多种微信相关的命名模式
        return lowerCaseId.contains("wechat") ||
               lowerCaseId.contains("weixin") ||
               lowerCaseId.startsWith("wx");
    }
}
