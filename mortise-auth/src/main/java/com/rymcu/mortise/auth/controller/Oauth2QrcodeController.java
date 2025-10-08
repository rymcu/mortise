package com.rymcu.mortise.auth.controller;

import com.rymcu.mortise.auth.support.UnifiedOAuth2AuthorizationRequestResolver;
import com.rymcu.mortise.core.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2025/10/8 22:33.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.controller
 */
@Tag(name = "OAuth2 授权二维码", description = "OAuth2 授权二维码生成接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/oauth2/qrcode")
@RequiredArgsConstructor
public class Oauth2QrcodeController {

    private final UnifiedOAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

    @Operation(
        summary = "获取微信 OAuth2 授权二维码链接",
        description = "根据 registration_id 获取微信 OAuth2 授权二维码的跳转链接。前端可跳转该链接进行扫码登录。",
        parameters = {
            @Parameter(name = "registration_id", description = "OAuth2 客户端注册ID", required = true, example = "wechat")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "操作成功，返回二维码授权链接。"),
            @ApiResponse(responseCode = "400", description = "请求参数错误或找不到客户端注册ID。")
        }
    )
    @GetMapping("/wechat/{registrationId}")
    public GlobalResult<Map<String, String>> getWeChatQRCode(@PathVariable String registrationId, HttpServletRequest request, HttpServletResponse response) {
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Unknown client registrationId: " + registrationId);
        }

        // 使用 Spring Security 的能力来构建标准的授权请求
        OAuth2AuthorizationRequest authRequest = authorizationRequestResolver.resolve(request, registrationId);
        if (authRequest == null) {
            throw new IllegalStateException("Could not resolve AuthorizationRequest for registrationId: " + registrationId);
        }

        // 2. 手动调用 save 方法，将 state 等信息存入缓存
        // 这一步是关键，它替代了被我们绕过的 OAuth2AuthorizationRequestRedirectFilter 的工作
        this.authorizationRequestRepository.saveAuthorizationRequest(authRequest, request, response);

        // 获取构建好的、包含 state 等参数的完整授权 URL
        String authorizationUri = authRequest.getAuthorizationRequestUri();
        Map<String, String> map = new HashMap<>();
        map.put("authorizationUri", authorizationUri);
        return GlobalResult.success(map);
    }
}
