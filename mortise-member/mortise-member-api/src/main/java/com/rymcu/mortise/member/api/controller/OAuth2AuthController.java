package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.model.QRCodeResult;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.service.QRCodeLoginService;
import com.rymcu.mortise.auth.support.UnifiedOAuth2AuthorizationRequestResolver;
import com.rymcu.mortise.auth.util.OAuth2ProviderUtils;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.member.api.model.OAuth2AuthUrlResponse;
import com.rymcu.mortise.member.api.model.OAuth2LoginResponse;
import com.rymcu.mortise.member.api.model.OAuth2QRCodeResponse;
import com.rymcu.mortise.member.api.model.OAuth2QRCodeStateResponse;
import com.rymcu.mortise.member.api.model.QRCodeLoginResponse;
import com.rymcu.mortise.member.api.service.OAuth2MemberBindingService;
import com.rymcu.mortise.web.annotation.ApiController;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * OAuth2 认证 Controller（客户端/会员端）
 * <p>
 * 提供 OAuth2 登录接口，支持：
 * - PC 端：获取二维码登录 URL（扫码登录）
 * - 手机端：获取授权页面重定向 URL（页面跳转授权）
 * - 查询授权状态
 * <p>
 * 回调处理由 Spring Security OAuth2 自动完成，成功后由 {@code ApiOAuth2LoginSuccessHandler} 处理
 *
 * @author ronger
 */
@Slf4j
@ApiController
@RequestMapping("/app/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2 认证", description = "OAuth2 登录相关接口（客户端）")
public class OAuth2AuthController {

    private final UnifiedOAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final AuthCacheService authCacheService;
    private final QRCodeLoginService qrCodeLoginService;
    private final OAuth2MemberBindingService oauth2MemberBindingService;

    @Operation(
            summary = "获取微信 OAuth2 二维码登录链接（PC 端扫码）",
            description = """
                    获取微信扫码登录的二维码 URL，适用于 PC 端扫码登录场景。\
                    该接口会生成一个唯一的 state 参数，用于防止 CSRF 攻击和状态管理。\
                    
                    
                    注意：此接口仅适用于 PC 端扫码登录，手机端请使用 /wechat/mobile/auth-url 接口。"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取二维码链接"),
            @ApiResponse(responseCode = "400", description = "请求参数错误或找不到客户端配置"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/wechat/qrcode")
    @ApiLog(value = "获取微信二维码登录链接", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<OAuth2QRCodeResponse> getWeChatQRCode(
            @Parameter(name = "registrationId", description = "OAuth2 客户端注册ID", example = "wechat-app")
            @RequestParam(value = "registrationId", defaultValue = "wechat-app") String registrationId,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("获取微信二维码登录链接，registrationId: {}", registrationId);

        // 1. 验证客户端注册是否存在
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Unknown client registrationId: " + registrationId);
        }

        // 2. 使用统一的授权请求解析器构建授权请求（包含微信特殊处理）
        OAuth2AuthorizationRequest authRequest = authorizationRequestResolver.resolve(request, registrationId);
        if (authRequest == null) {
            throw new IllegalStateException("Could not resolve AuthorizationRequest for registrationId: " + registrationId);
        }

        // 3. 保存授权请求到 Repository（替代 OAuth2AuthorizationRequestRedirectFilter 的工作）
        authorizationRequestRepository.saveAuthorizationRequest(authRequest, request, response);

        // 4. 存储二维码状态
        String state = authRequest.getState();
        if (StringUtils.isNotBlank(state)) {
            authCacheService.storeOAuth2QrcodeState(state, QrcodeState.WAITED.getValue());
        }

        // 5. 构建响应
        OAuth2QRCodeResponse qrCodeResponse = new OAuth2QRCodeResponse(
                authRequest.getAuthorizationRequestUri(),
                state,
                authRequest.getClientId(),
                authRequest.getRedirectUri(),
                String.join(",", authRequest.getScopes())
        );

        return GlobalResult.success(qrCodeResponse);
    }

    @Operation(
            summary = "查询授权状态（扫码/重定向通用）",
            description = """
                    查询 OAuth2 授权进度，支持 PC 端扫码和手机端重定向两种场景。\
                    前端可轮询此接口查询登录状态，建议轮询间隔 1-2 秒。
                    
                    返回的状态值含义：
                    - 0: 待授权/待扫描 - 用户尚未扫描二维码或授权
                    - 1: 已扫码 - 用户已扫描但未确认登录
                    - 2: 已授权 - 用户已确认登录
                    - 3: 已取消 - 用户取消登录
                    - 4: 已过期 - 二维码或授权已过期
                    - -1: 状态不存在或已清理
                    
                    注意：此接口适用于所有 OAuth2 登录场景，包括微信公众号扫码登录。"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功查询状态"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/qrcode/state/{state}")
    @ApiLog(value = "查询OAuth2授权状态", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<OAuth2QRCodeStateResponse> getQRCodeState(
            @Parameter(name = "state", description = "状态码或场景值（sceneStr）", required = true)
            @PathVariable String state) {

        log.debug("查询二维码状态: state={}", state);

        int qrcodeState = authCacheService.getOAuth2QrcodeState(state);

        OAuth2QRCodeStateResponse result = new OAuth2QRCodeStateResponse(state, qrcodeState);

        return GlobalResult.success(result);
    }

    @Operation(
            summary = "获取微信 OAuth2 授权 URL（手机端页面重定向）",
            description = """
                    获取手机端微信授权 URL，适用于在微信内打开的 H5 页面。\
                    用户点击后将跳转到微信授权页面，授权完成后重定向回业务页面。\
                    
                    
                    与 PC 端扫码登录的区别：
                    - PC 端使用 snsapi_login 作用域，需要扫码
                    - 手机端使用 snsapi_userinfo 或 snsapi_base 作用域，通过页面重定向授权
                    
                    
                    scope 参数说明：
                    - snsapi_base：静默授权，不弹出授权页面，只能获取用户 openid
                    - snsapi_userinfo：弹出授权页面，可获取用户昵称、头像等信息"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取授权 URL"),
            @ApiResponse(responseCode = "400", description = "请求参数错误或找不到客户端配置"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/wechat/mobile/auth-url")
    @ApiLog(value = "获取微信OAuth2授权URL(手机端)", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<OAuth2AuthUrlResponse> getMobileAuthUrl(
            @Parameter(name = "registrationId", description = "OAuth2 客户端注册ID", example = "wechat-app")
            @RequestParam(value = "registrationId", defaultValue = "wechat-app") String registrationId,
            @Parameter(name = "scope", description = "授权作用域：snsapi_base（静默授权）或 snsapi_userinfo（弹出授权页面）",
                    example = "snsapi_userinfo")
            @RequestParam(value = "scope", defaultValue = "snsapi_userinfo") String scope,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.debug("获取手机端微信授权 URL，registrationId: {}, scope: {}", registrationId, scope);

        // 1. 验证客户端注册是否存在
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Unknown client registrationId: " + registrationId);
        }

        // 2. 使用统一的授权请求解析器构建授权请求
        OAuth2AuthorizationRequest authRequest = authorizationRequestResolver.resolve(request, registrationId);
        if (authRequest == null) {
            throw new IllegalStateException("Could not resolve AuthorizationRequest for registrationId: " + registrationId);
        }

        // 3. 保存授权请求到 Repository
        authorizationRequestRepository.saveAuthorizationRequest(authRequest, request, response);

        // 4. 存储状态
        String state = authRequest.getState();
        if (StringUtils.isNotBlank(state)) {
            authCacheService.storeOAuth2QrcodeState(state, QrcodeState.WAITED.getValue());
        }

        // 5. 判断授权类型
        String authType = OAuth2ProviderUtils.isWeChatProvider(registrationId) ? "wechat_redirect" : "standard";

        // 6. 构建响应
        OAuth2AuthUrlResponse authUrlResponse = new OAuth2AuthUrlResponse(
                authRequest.getAuthorizationRequestUri(),
                state,
                authRequest.getClientId(),
                authRequest.getRedirectUri(),
                scope,
                authType
        );

        return GlobalResult.success(authUrlResponse);
    }

    @Operation(
            summary = "获取 OAuth2 授权 URL（PC 端直链方式）",
            description = "直接返回 PC 端授权 URL，适用于需要自定义跳转逻辑的场景。"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取授权 URL"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/auth-url/{registrationId}")
    @ApiLog(value = "获取OAuth2授权URL", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<OAuth2AuthUrlResponse> getAuthUrl(
            @Parameter(name = "registrationId", description = "OAuth2 客户端注册ID", example = "wechat-app")
            @PathVariable String registrationId,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.debug("获取授权 URL，registrationId: {}", registrationId);

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Unknown client registrationId: " + registrationId);
        }

        OAuth2AuthorizationRequest authRequest = authorizationRequestResolver.resolve(request, registrationId);
        if (authRequest == null) {
            throw new IllegalStateException("Could not resolve AuthorizationRequest for registrationId: " + registrationId);
        }

        authorizationRequestRepository.saveAuthorizationRequest(authRequest, request, response);

        String state = authRequest.getState();
        if (StringUtils.isNotBlank(state)) {
            authCacheService.storeOAuth2QrcodeState(state, QrcodeState.WAITED.getValue());
        }

        OAuth2AuthUrlResponse result = new OAuth2AuthUrlResponse(
                authRequest.getAuthorizationRequestUri(),
                state,
                null,
                null,
                null,
                null
        );

        return GlobalResult.success(result);
    }

    /**
     * 兑换 Token
     */
    @Operation(summary = "兑换 Token", description = "使用 state 兑换 Token（与 SystemAuthController 保持一致）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "兑换成功"),
            @ApiResponse(responseCode = "401", description = "认证失败"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @GetMapping("/callback")
    @ApiLog(value = "OAuth2兑换Token(客户端)", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "OAuth2认证", operation = "OAuth2兑换Token(客户端)", recordParams = false, recordResult = false)
    public GlobalResult<OAuth2LoginResponse> oauth2Login(
            @Parameter(description = "兑换 Token 请求", required = true)
            @RequestParam("state") String state) {
        log.info("API 客户端兑换 Token");

        OAuth2LoginResponse loginResponse = authCacheService.getOAuth2LoginResponse(state, OAuth2LoginResponse.class);
        if (loginResponse == null) {
            return GlobalResult.error("无效的 state 或已过期");
        }

        // 删除已使用的缓存
        authCacheService.removeOAuth2LoginResponse(state);
        authCacheService.removeOAuth2QrcodeState(state);

        return GlobalResult.success(loginResponse);
    }

    @Operation(
            summary = "创建微信扫码登录二维码",
            description = """
                    创建一个新的扫码登录二维码，返回二维码 URL 和场景值。
                    
                    前端可以使用返回的 qrCodeUrl 或 showQrCodeUrl 展示二维码：
                    - qrCodeUrl: 微信返回的原始 URL，可用于生成二维码图片
                    - showQrCodeUrl: 微信提供的二维码图片 URL，可直接显示
                    
                    二维码默认有效期 5 分钟，过期后需要重新创建。"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功创建二维码"),
            @ApiResponse(responseCode = "500", description = "创建二维码失败")
    })
    @PostMapping("/mp/qrcode")
    @ApiLog(value = "创建微信扫码登录二维码", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<QRCodeLoginResponse> createWeChatQRCode(
            @Parameter(description = "二维码有效期（秒），范围：60-300，默认 300")
            @RequestParam(value = "expireSeconds", defaultValue = "300") Integer expireSeconds,
            @RequestParam(value = "appId", defaultValue = "") String appId) {

        log.info("创建微信扫码登录二维码 - appId: {} - expireSeconds: {}", appId, expireSeconds);

        // 检查服务是否可用
        if (qrCodeLoginService == null) {
            log.warn("微信扫码登录服务未启用");
            return GlobalResult.error("微信扫码登录功能未启用，请确保已加载 mortise-wechat 模块");
        }

        // 参数校验
        if (expireSeconds < 60 || expireSeconds > 300) {
            return GlobalResult.error("二维码有效期必须在 60-300 秒之间");
        }

        try {
            // 生成唯一场景值
            String sceneStr = "LOGIN_" + UUID.randomUUID().toString().replace("-", "");

            // 调用服务创建二维码
            QRCodeResult result = qrCodeLoginService.createQRCode(appId, sceneStr, expireSeconds);

            // 构建响应
            QRCodeLoginResponse response = new QRCodeLoginResponse(
                    sceneStr,
                    result.getUrl(),
                    result.getTicket(),
                    result.getExpireSeconds(),
                    buildShowQrCodeUrl(result.getTicket())
            );

            log.info("微信扫码登录二维码创建成功 - sceneStr: {}", sceneStr);
            return GlobalResult.success(response);

        } catch (Exception e) {
            log.error("创建微信扫码登录二维码失败", e);
            return GlobalResult.error("创建二维码失败: " + e.getMessage());
        }
    }


    // ==================== 内部方法 ====================

    /**
     * 构建微信二维码图片显示 URL
     * <p>
     * 微信提供的图片 URL 格式：<a href="https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET">...</a>
     * <p>
     * 注意：ticket 需要进行 URL 编码
     *
     * @param ticket 二维码 ticket
     * @return 二维码图片 URL
     */

    private String buildShowQrCodeUrl(String ticket) {
        if (ticket == null || ticket.isEmpty()) {
            return null;
        }
        try {
            String encodedTicket = URLEncoder.encode(ticket, StandardCharsets.UTF_8);
            return "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + encodedTicket;
        } catch (Exception e) {
            log.error("构建二维码显示 URL 失败", e);
            return null;
        }
    }

}
