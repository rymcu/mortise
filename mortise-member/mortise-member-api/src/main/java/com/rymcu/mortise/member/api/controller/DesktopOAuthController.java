package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.member.api.model.DesktopAuthorizeResponse;
import com.rymcu.mortise.member.api.model.DesktopLogoutRequest;
import com.rymcu.mortise.member.api.model.DesktopTokenRequest;
import com.rymcu.mortise.member.api.model.DesktopTokenResponse;
import com.rymcu.mortise.member.api.service.DesktopOAuthService;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 桌面端授权桥 Controller。
 *
 * @author ronger
 */
@ApiController
@RequestMapping("/app/desktop/oauth")
@RequiredArgsConstructor
@Tag(name = "桌面端授权", description = "Rodak 桌面端浏览器授权登录接口")
public class DesktopOAuthController {

    private final DesktopOAuthService desktopOAuthService;

    @GetMapping("/authorize")
    @Operation(summary = "创建桌面端授权码")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "创建桌面端授权码")
    public GlobalResult<DesktopAuthorizeResponse> authorize(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("state") String state,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam("code_challenge_method") String codeChallengeMethod,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "device_name", required = false) String deviceName,
            @RequestParam(value = "device_fingerprint", required = false) String deviceFingerprint) {
        String callback = desktopOAuthService.authorize(
                currentUser,
                clientId,
                redirectUri,
                state,
                codeChallenge,
                codeChallengeMethod,
                scope,
                deviceName,
                deviceFingerprint
        );
        return GlobalResult.success(new DesktopAuthorizeResponse(callback));
    }

    @PostMapping("/token")
    @Operation(summary = "桌面端授权码换取 Token")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "桌面端换取Token")
    public GlobalResult<DesktopTokenResponse> token(@Valid @RequestBody DesktopTokenRequest request) {
        return GlobalResult.success(desktopOAuthService.token(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "撤销桌面端会话")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "桌面端退出登录")
    public GlobalResult<Boolean> logout(@AuthenticationPrincipal CurrentUser currentUser,
                                        @RequestBody(required = false) DesktopLogoutRequest request,
                                        @RequestParam(value = "refreshToken", required = false) String refreshToken,
                                        @RequestParam(value = "sessionId", required = false) Long sessionId) {
        String resolvedRefreshToken = request != null && request.refreshToken() != null
                ? request.refreshToken()
                : refreshToken;
        Long resolvedSessionId = request != null && request.sessionId() != null
                ? request.sessionId()
                : sessionId;
        return GlobalResult.success(desktopOAuthService.logout(currentUser, resolvedRefreshToken, resolvedSessionId));
    }
}
