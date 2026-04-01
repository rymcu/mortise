package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.member.api.facade.MemberAuthFacade;
import com.rymcu.mortise.member.api.model.MemberLoginRequest;
import com.rymcu.mortise.member.api.model.MemberLoginResponse;
import com.rymcu.mortise.member.api.model.MemberPasswordRequest;
import com.rymcu.mortise.member.api.model.MemberRegisterRequest;
import com.rymcu.mortise.member.api.model.MemberUpdateRequest;
import com.rymcu.mortise.member.api.model.MemberUsernameRequest;
import com.rymcu.mortise.member.api.model.PhoneLoginRequest;
import com.rymcu.mortise.member.api.model.RefreshTokenRequest;
import com.rymcu.mortise.member.api.model.ResetPasswordRequest;
import com.rymcu.mortise.member.api.model.SendCodeRequest;
import com.rymcu.mortise.member.api.model.TokenRefreshResponse;
import com.rymcu.mortise.member.api.model.VerifyCodeRequest;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 会员认证 Controller（客户端）
 *
 * @author ronger
 */
@ApiController
@RequestMapping("/app/auth")
@RequiredArgsConstructor
@Tag(name = "会员认证", description = "会员注册、登录等接口")
public class MemberAuthController {

    private final MemberAuthFacade memberAuthFacade;

    @PostMapping("/register")
    @Operation(summary = "会员注册")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "会员注册")
    @OperationLog(module = "会员认证", operation = "会员注册", recordParams = false, recordResult = true)
    public GlobalResult<Long> register(@Valid @RequestBody MemberRegisterRequest request) {
        return memberAuthFacade.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "会员登录")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "会员登录")
    @OperationLog(module = "会员认证", operation = "会员登录", recordParams = false, recordResult = false)
    public GlobalResult<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        return memberAuthFacade.login(request);
    }

    @PostMapping("/login-by-phone")
    @Operation(summary = "手机号验证码登录", description = "使用手机号和验证码登录，无需密码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "手机号验证码登录")
    @OperationLog(module = "会员认证", operation = "手机号验证码登录", recordParams = false, recordResult = false)
    public GlobalResult<MemberLoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return memberAuthFacade.loginByPhone(request);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新 Token（使用 refreshToken）", description = "使用 refreshToken 获取新的访问令牌")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "刷新 Token")
    public GlobalResult<MemberLoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return memberAuthFacade.refreshToken(request);
    }

    @PostMapping("/refresh-token-by-jwt")
    @Operation(summary = "刷新 Token（使用 JWT 窗口期）", description = "在 JWT 刷新窗口期内刷新 Token（兼容旧版）")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "刷新Token（JWT窗口期）")
    public GlobalResult<TokenRefreshResponse> refreshTokenByJwt(@RequestHeader(JwtConstants.AUTHORIZATION) String authHeader) {
        return memberAuthFacade.refreshTokenByJwt(authHeader);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前会员信息")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "获取当前会员信息")
    public GlobalResult<Member> getProfile(@AuthenticationPrincipal CurrentUser currentUser) {
        return memberAuthFacade.getProfile(currentUser);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新会员信息")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "更新会员信息")
    @OperationLog(module = "会员信息", operation = "更新会员信息", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> updateProfile(@AuthenticationPrincipal CurrentUser currentUser,
                                               @Valid @RequestBody MemberUpdateRequest request) {
        return memberAuthFacade.updateProfile(currentUser, request);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "修改密码")
    @OperationLog(module = "会员信息", operation = "修改密码", recordParams = false, recordResult = false)
    public GlobalResult<Boolean> updatePassword(@AuthenticationPrincipal CurrentUser currentUser,
                                                @Valid @RequestBody MemberPasswordRequest request) {
        return memberAuthFacade.updatePassword(currentUser, request);
    }

    @PutMapping("/username")
    @Operation(summary = "修改用户名")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "修改用户名")
    @OperationLog(module = "会员信息", operation = "修改用户名", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> updateUsername(@AuthenticationPrincipal CurrentUser currentUser,
                                                @Valid @RequestBody MemberUsernameRequest request) {
        return memberAuthFacade.updateUsername(currentUser, request);
    }

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "发送验证码")
    public GlobalResult<Boolean> sendCode(@Valid @RequestBody SendCodeRequest request) {
        return memberAuthFacade.sendCode(request);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "验证验证码后重置密码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "重置密码")
    public GlobalResult<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return memberAuthFacade.resetPassword(request);
    }

    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "验证验证码")
    public GlobalResult<Boolean> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return memberAuthFacade.verifyCode(request);
    }
}
