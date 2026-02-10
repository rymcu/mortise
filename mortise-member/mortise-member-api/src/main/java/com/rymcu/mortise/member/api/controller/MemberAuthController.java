package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.member.api.model.MemberLoginRequest;
import com.rymcu.mortise.member.api.model.MemberLoginResponse;
import com.rymcu.mortise.member.api.model.MemberPasswordRequest;
import com.rymcu.mortise.member.api.model.MemberRegisterRequest;
import com.rymcu.mortise.member.api.model.MemberUpdateRequest;
import com.rymcu.mortise.member.api.model.MemberUsernameRequest;
import com.rymcu.mortise.member.api.model.PhoneLoginRequest;
import com.rymcu.mortise.member.api.model.RefreshTokenRequest;
import com.rymcu.mortise.member.api.model.SendCodeRequest;
import com.rymcu.mortise.member.api.model.TokenRefreshResponse;
import com.rymcu.mortise.member.api.model.VerifyCodeRequest;
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.api.service.MemberContextService;
import com.rymcu.mortise.member.api.service.VerificationCodeService;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员认证 Controller（客户端）
 *
 * @author ronger
 */
@Slf4j
@ApiController
@RequestMapping("/app/auth")
@RequiredArgsConstructor
@Tag(name = "会员认证", description = "会员注册、登录等接口")
public class MemberAuthController {

    private final ApiMemberService memberService;
    private final JwtTokenUtil jwtTokenUtil;
    private final VerificationCodeService verificationCodeService;
    private final MemberContextService memberContextService;
    private final AuthCacheService authCacheService;

    @PostMapping("/register")
    @Operation(summary = "会员注册")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "会员注册")
    @OperationLog(module = "会员认证", operation = "会员注册", recordParams = false, recordResult = true)
    public GlobalResult<Long> register(@Valid @RequestBody MemberRegisterRequest request) {
        try {
            Member member = new Member();
            member.setUsername(request.username());
            member.setEmail(request.email());
            member.setPhone(request.phone());
            member.setNickname(request.nickname());

            Long memberId = memberService.register(member, request.password());

            log.info("会员注册成功: memberId={}", memberId);

            return GlobalResult.success(memberId);
        } catch (Exception e) {
            log.error("会员注册失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "会员登录")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "会员登录")
    @OperationLog(module = "会员认证", operation = "会员登录", recordParams = false, recordResult = false)
    public GlobalResult<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        Member member = memberService.login(request.account(), request.password());

        // 生成 JWT Token（添加 memberId 到 claims）
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", member.getId());
        claims.put("type", "member"); // 标识为会员 token

        // 使用 username 作为 subject（JWT 的标准用法）
        String username = member.getUsername();
        if (username == null || username.trim().isEmpty()) {
            // 如果 username 为空，使用 email 或 phone 作为 fallback
            username = member.getEmail() != null ? member.getEmail() : member.getPhone();
        }

        String jwtToken = jwtTokenUtil.generateToken(username, claims);

        // 生成 Refresh Token 并存储
        String refreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(refreshToken, member.getId());

        MemberLoginResponse response = new MemberLoginResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getAvatarUrl(),
                jwtToken,
                refreshToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                1800000L,
                AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS * 60 * 60 * 1000
        );

        log.info("会员登录成功: memberId={}", member.getId());

        return GlobalResult.success(response);
    }

    @PostMapping("/login-by-phone")
    @Operation(summary = "手机号验证码登录", description = "使用手机号和验证码登录，无需密码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "手机号验证码登录")
    @OperationLog(module = "会员认证", operation = "手机号验证码登录", recordParams = false, recordResult = false)
    public GlobalResult<MemberLoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        Member member = memberService.loginByPhone(request.phone(), request.code());

        // 生成 JWT Token（添加 memberId 到 claims）
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", member.getId());
        claims.put("type", "member"); // 标识为会员 token
        claims.put("loginType", "phone"); // 标识登录方式

        // 使用手机号作为 subject
        String username = member.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = member.getPhone();
        }

        String jwtToken = jwtTokenUtil.generateToken(username, claims);

        // 生成 Refresh Token 并存储
        String refreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(refreshToken, member.getId());

        MemberLoginResponse response = new MemberLoginResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getAvatarUrl(),
                jwtToken,
                refreshToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                1800000L,
                AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS * 60 * 60 * 1000
        );

        log.info("手机号验证码登录成功: memberId={}", member.getId());

        return GlobalResult.success(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新 Token（使用 refreshToken）", description = "使用 refreshToken 获取新的访问令牌")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "刷新 Token")
    public GlobalResult<MemberLoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token 刷新请求");

        // 从缓存中获取会员 ID
        Long memberId = authCacheService.getMemberIdByRefreshToken(request.refreshToken());
        if (memberId == null) {
            log.warn("无效的 refreshToken 或已过期");
            return GlobalResult.error("无效的 refreshToken 或已过期");
        }

        // 获取会员信息
        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            log.warn("Token刷新失败: 会员不存在, memberId={}", memberId);
            return GlobalResult.error("会员不存在");
        }

        // 生成新的 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", member.getId());
        claims.put("type", "member");

        String username = member.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = member.getEmail() != null ? member.getEmail() : member.getPhone();
        }

        String jwtToken = jwtTokenUtil.generateToken(username, claims);

        // 删除旧的 refreshToken，生成新的
        authCacheService.removeMemberRefreshToken(request.refreshToken());
        String newRefreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(newRefreshToken, member.getId());

        MemberLoginResponse response = new MemberLoginResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getAvatarUrl(),
                jwtToken,
                newRefreshToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                1800000L,
                AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS * 60 * 60 * 1000
        );

        log.info("Token 刷新成功: memberId={}", member.getId());

        return GlobalResult.success(response);
    }

    @PostMapping("/refresh-token-by-jwt")
    @Operation(summary = "刷新 Token（使用 JWT 窗口期）", description = "在 JWT 刷新窗口期内刷新 Token（兼容旧版）")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "刷新Token（JWT窗口期）")
    public GlobalResult<TokenRefreshResponse> refreshTokenByJwt(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith(jwtTokenUtil.getTokenPrefix())) {
            return GlobalResult.error("无效的 Token 格式");
        }

        String token = authHeader.substring(jwtTokenUtil.getTokenPrefix().length());
        String newToken = jwtTokenUtil.refreshToken(token);

        if (newToken == null) {
            return GlobalResult.error("Token 刷新失败或尚未到刷新窗口期");
        }

        TokenRefreshResponse response = new TokenRefreshResponse(
                newToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                1800000L
        );

        return GlobalResult.success(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前会员信息")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "获取当前会员信息")
    public GlobalResult<Member> getProfile() {
        Member member = memberService.getMemberById(memberContextService.getCurrentMemberId());
        if (member == null) {
            return GlobalResult.error("未登录");
        }

        // 清除敏感信息
        member.setPasswordHash(null);
        return GlobalResult.success(member);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新会员信息")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "更新会员信息")
    @OperationLog(module = "会员信息", operation = "更新会员信息", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> updateProfile(@Valid @RequestBody MemberUpdateRequest request) {
        Long memberId = memberContextService.getCurrentMemberId();
        if (memberId == null) {
            return GlobalResult.error("未登录");
        }

        log.info("更新会员信息请求: memberId={}", memberId);

        Member member = new Member();
        member.setId(memberId);
        member.setNickname(request.nickname());
        member.setAvatarUrl(request.avatarUrl());
        member.setGender(request.gender());
        member.setBirthDate(request.birthDate());

        Boolean success = memberService.updateMember(member);
        return GlobalResult.success(success);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "修改密码")
    @OperationLog(module = "会员信息", operation = "修改密码", recordParams = false, recordResult = false)
    public GlobalResult<Boolean> updatePassword(@Valid @RequestBody MemberPasswordRequest request) {
        Long memberId = memberContextService.getCurrentMemberId();
        if (memberId == null) {
            return GlobalResult.error("未登录");
        }

        log.info("修改密码请求: memberId={}", memberId);

        Boolean success = memberService.updatePassword(memberId, request.oldPassword(), request.newPassword());

        if (success) {
            log.info("密码修改成功: memberId={}", memberId);
        } else {
            log.warn("密码修改失败: memberId={}", memberId);
        }

        return GlobalResult.success(success);
    }

    @PutMapping("/username")
    @Operation(summary = "修改用户名")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "修改用户名")
    @OperationLog(module = "会员信息", operation = "修改用户名", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> updateUsername(@Valid @RequestBody MemberUsernameRequest request) {
        Long memberId = memberContextService.getCurrentMemberId();
        if (memberId == null) {
            return GlobalResult.error("未登录");
        }

        log.info("修改用户名请求: memberId={}, newUsername={}", memberId, request.newUsername());

        Boolean success = memberService.updateUsername(memberId, request.newUsername());

        if (success) {
            log.info("用户名修改成功: memberId={}, newUsername={}", memberId, request.newUsername());
        } else {
            log.warn("用户名修改失败: memberId={}", memberId);
        }

        return GlobalResult.success(success);
    }

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "发送验证码")
    public GlobalResult<Boolean> sendCode(@Valid @RequestBody SendCodeRequest request) {
        Boolean success;

        if ("sms".equalsIgnoreCase(request.type())) {
            if (request.phone() == null || request.phone().trim().isEmpty()) {
                return GlobalResult.error("手机号不能为空");
            }
            success = verificationCodeService.sendSmsCode(request.phone());
        } else if ("email".equalsIgnoreCase(request.type())) {
            if (request.email() == null || request.email().trim().isEmpty()) {
                return GlobalResult.error("邮箱不能为空");
            }
            success = verificationCodeService.sendEmailCode(request.email());
        } else {
            return GlobalResult.error("不支持的验证码类型: " + request.type());
        }

        return GlobalResult.success(success);
    }

    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "验证验证码")
    public GlobalResult<Boolean> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        Boolean isValid;

        if ("sms".equalsIgnoreCase(request.type())) {
            if (request.phone() == null || request.phone().trim().isEmpty()) {
                return GlobalResult.error("手机号不能为空");
            }
            isValid = verificationCodeService.verifySmsCode(request.phone(), request.code());
        } else if ("email".equalsIgnoreCase(request.type())) {
            if (request.email() == null || request.email().trim().isEmpty()) {
                return GlobalResult.error("邮箱不能为空");
            }
            isValid = verificationCodeService.verifyEmailCode(request.email(), request.code());
        } else {
            return GlobalResult.error("不支持的验证码类型: " + request.type());
        }

        if (!isValid) {
            return GlobalResult.error("验证码错误或已过期");
        }

        return GlobalResult.success(true);
    }
}
