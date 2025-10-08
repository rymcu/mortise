package com.rymcu.mortise.auth.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

/**
 * 微信 OAuth2 AccessToken 响应客户端
 * <p>
 * 专门处理微信 OAuth2 授权码模式的 token 获取，解决以下问题：
 * <ol>
 *   <li>微信 API 返回 Content-Type 为 text/plain 而非标准的 application/json</li>
 *   <li>微信响应缺少必需的 token_type 字段，需要手动补充</li>
 *   <li>提供完整的错误处理和日志记录</li>
 * </ol>
 * <p>
 * <b>使用方式</b>：
 * <pre>{@code
 * @Bean
 * public SecurityFilterChain securityFilterChain(HttpSecurity http,
 *     WeChatAccessTokenResponseClient weChatTokenClient) throws Exception {
 *     http.oauth2Login(oauth2 -> oauth2
 *         .tokenEndpoint(token -> token
 *             .accessTokenResponseClient(weChatTokenClient)
 *         )
 *     );
 *     return http.build();
 * }
 * }</pre>
 * <p>
 * 该组件是可选的，可以通过配置项 mortise.oauth2.wechat.custom-components-enabled 控制是否启用
 *
 * @author ronger
 * @since 1.0.0
 * @see OAuth2AccessTokenResponseClient
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "mortise.oauth2.wechat",
        name = "custom-components-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public final class WeChatAccessTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestOperations restOperations;
    private final Converter<Map<String, Object>, OAuth2AccessTokenResponse> responseConverter;

    /**
     * 构造函数，初始化专门用于微信的 RestTemplate 和转换器
     */
    public WeChatAccessTokenResponseClient(@Qualifier("weChatRestTemplate") RestTemplate weChatRestTemplate) {
        this.restOperations = weChatRestTemplate;
        this.responseConverter = new DefaultMapOAuth2AccessTokenResponseConverter();

        log.info("初始化 WeChatAccessTokenResponseClient，支持 text/plain 响应类型");
    }

    /**
     * 获取 OAuth2 访问令牌响应
     *
     * @param authorizationCodeGrantRequest 授权码请求
     * @return OAuth2 访问令牌响应
     * @throws OAuth2AuthenticationException 如果请求失败或响应无效
     */
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(
            OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {

        Assert.notNull(authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null");

        String registrationId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
        log.debug("正在为客户端 [{}] 获取访问令牌", registrationId);

        // 1. 构建 HTTP 请求（不使用已弃用的 Converter）
        RequestEntity<?> request = buildTokenRequest(authorizationCodeGrantRequest);

        try {
            // 2. 发送 HTTP 请求获取 token
            Map<String, Object> responseMap = UnifiedOAuth2AccessTokenResponseClient.exchangeResponseEntity(request, this.restOperations, RESPONSE_TYPE);

            // 3. 检查微信特定的错误码
            if (responseMap.containsKey("errcode")) {
                int errCode = ((Number) responseMap.get("errcode")).intValue();
                String errMsg = (String) responseMap.getOrDefault("errmsg", "Unknown error");
                log.error("微信 Token 请求失败 - errcode: {}, errmsg: {}", errCode, errMsg);

                throw new OAuth2AuthenticationException(
                    new OAuth2Error("wechat_error",
                        String.format("WeChat API error: %d - %s", errCode, errMsg), null)
                );
            }

            // 4. 修复缺失的 token_type（微信不返回此字段）
            if (!responseMap.containsKey(OAuth2ParameterNames.TOKEN_TYPE)) {
                responseMap.put(OAuth2ParameterNames.TOKEN_TYPE, OAuth2AccessToken.TokenType.BEARER.getValue());
                log.debug("已为微信响应补充 token_type 字段");
            }

            // 5. 转换为标准的 OAuth2AccessTokenResponse
            OAuth2AccessTokenResponse tokenResponse = this.responseConverter.convert(responseMap);

            if (tokenResponse == null) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token_response", "Failed to convert token response", null)
                );
            }

            log.info("成功获取客户端 [{}] 的访问令牌", registrationId);
            return tokenResponse;

        } catch (OAuth2AuthenticationException ex) {
            // 直接抛出 OAuth2 认证异常
            throw ex;

        } catch (RestClientException ex) {
            // 包装其他异常
            log.error("获取访问令牌时发生网络错误", ex);
            OAuth2Error oauth2Error = new OAuth2Error(
                "invalid_token_response",
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
                        + ex.getMessage(),
                null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);

        } catch (Exception ex) {
            // 处理未预期的异常
            log.error("获取访问令牌时发生未知错误", ex);
            OAuth2Error oauth2Error = new OAuth2Error(
                "server_error",
                "An unexpected error occurred: " + ex.getMessage(),
                null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
    }

    /**
     * 构建 OAuth2 Token 请求
     * <p>
     * 手动构建请求以避免使用已弃用的 OAuth2AuthorizationCodeGrantRequestEntityConverter
     *
     * @param grantRequest 授权码授权请求
     * @return HTTP 请求实体
     */
    private RequestEntity<?> buildTokenRequest(OAuth2AuthorizationCodeGrantRequest grantRequest) {
        // 获取客户端注册信息
        ClientRegistration clientRegistration = grantRequest.getClientRegistration();
        OAuth2AuthorizationExchange authorizationExchange = grantRequest.getAuthorizationExchange();

        // 构建请求参数
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add(OAuth2ParameterNames.GRANT_TYPE, grantRequest.getGrantType().getValue());
        parameters.add(OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());

        // 添加 redirect_uri（如果存在）
        String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();
        if (redirectUri != null) {
            parameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
        }

        // 添加客户端认证信息
        parameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
        // 添加微信标识 appid
        parameters.add("appid", clientRegistration.getClientId());

        // 微信使用 client_secret_post 方式，将 secret 放在 body 中
        if (clientRegistration.getClientSecret() != null) {
            parameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
            parameters.add("secret", clientRegistration.getClientSecret());
        }

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        // 获取 Token URI
        URI tokenUri = UriComponentsBuilder
            .fromUriString(clientRegistration.getProviderDetails().getTokenUri())
            .build()
            .toUri();

        // 构建并返回请求
        return new RequestEntity<>(parameters, headers, HttpMethod.POST, tokenUri);
    }
}
