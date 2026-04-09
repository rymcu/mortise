package com.rymcu.mortise.member.api.facade.impl;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
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
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.api.service.VerificationCodeService;
import com.rymcu.mortise.member.constant.MemberJwtConstants;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.enumerate.VerificationCodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MemberAuthFacadeImpl implements MemberAuthFacade {

    private final ApiMemberService memberService;
    private final JwtTokenUtil jwtTokenUtil;
    private final VerificationCodeService verificationCodeService;
    private final AuthCacheService authCacheService;

    public MemberAuthFacadeImpl(ApiMemberService memberService,
                                JwtTokenUtil jwtTokenUtil,
                                VerificationCodeService verificationCodeService,
                                AuthCacheService authCacheService) {
        this.memberService = memberService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.verificationCodeService = verificationCodeService;
        this.authCacheService = authCacheService;
    }

    @Override
    public GlobalResult<Long> register(MemberRegisterRequest request) {
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

    @Override
    public GlobalResult<MemberLoginResponse> login(MemberLoginRequest request) {
        Member member = memberService.login(request.account(), request.password());
        MemberLoginResponse response = buildLoginResponse(member);
        log.info("会员登录成功: memberId={}", member.getId());
        return GlobalResult.success(response);
    }

    @Override
    public GlobalResult<MemberLoginResponse> loginByPhone(PhoneLoginRequest request) {
        Member member = memberService.loginByPhone(request.phone(), request.code());
        MemberLoginResponse response = buildLoginResponse(
                member,
                MemberJwtConstants.CLAIM_LOGIN_TYPE,
                MemberJwtConstants.LOGIN_TYPE_PHONE
        );
        log.info("手机号验证码登录成功: memberId={}", member.getId());
        return GlobalResult.success(response);
    }

    @Override
    public GlobalResult<MemberLoginResponse> refreshToken(RefreshTokenRequest request) {
        log.debug("Token 刷新请求");
        Long memberId = authCacheService.getMemberIdByRefreshToken(request.refreshToken());
        if (memberId == null) {
            log.warn("无效的 refreshToken 或已过期");
            return GlobalResult.error("无效的 refreshToken 或已过期");
        }

        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            log.warn("Token刷新失败: 会员不存在, memberId={}", memberId);
            return GlobalResult.error("会员不存在");
        }

        authCacheService.removeMemberRefreshToken(request.refreshToken());
        Map<String, Object> claims = buildBaseClaims(member);
        String jwtToken = jwtTokenUtil.generateToken(resolveSubject(member), claims);
        String newRefreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(newRefreshToken, member.getId());

        MemberLoginResponse response = buildLoginResponse(member, jwtToken, newRefreshToken);
        log.info("Token 刷新成功: memberId={}", member.getId());
        return GlobalResult.success(response);
    }

    @Override
    public GlobalResult<TokenRefreshResponse> refreshTokenByJwt(String authHeader) {
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
                MemberJwtConstants.ACCESS_TOKEN_EXPIRY_MS
        );
        return GlobalResult.success(response);
    }

    @Override
    public GlobalResult<Member> getProfile(CurrentUser currentUser) {
        if (currentUser == null) {
            return GlobalResult.error("未登录");
        }
        Member member = memberService.getMemberById(currentUser.getUserId());
        if (member == null) {
            return GlobalResult.error("未登录");
        }
        member.setPasswordHash(null);
        return GlobalResult.success(member);
    }

    @Override
    public GlobalResult<Boolean> updateProfile(CurrentUser currentUser, MemberUpdateRequest request) {
        if (currentUser == null) {
            return GlobalResult.error("未登录");
        }
        Long memberId = currentUser.getUserId();
        log.info("更新会员信息请求: memberId={}", memberId);

        Member member = new Member();
        member.setId(memberId);
        member.setNickname(request.nickname());
        member.setAvatarUrl(request.avatarUrl());
        member.setGender(request.gender());
        member.setBirthDate(request.birthDate());

        return GlobalResult.success(memberService.updateMember(member));
    }

    @Override
    public GlobalResult<Boolean> updatePassword(CurrentUser currentUser, MemberPasswordRequest request) {
        if (currentUser == null) {
            return GlobalResult.error("未登录");
        }
        Long memberId = currentUser.getUserId();
        log.info("修改密码请求: memberId={}", memberId);
        Boolean success = memberService.updatePassword(memberId, request.oldPassword(), request.newPassword());
        if (Boolean.TRUE.equals(success)) {
            log.info("密码修改成功: memberId={}", memberId);
        } else {
            log.warn("密码修改失败: memberId={}", memberId);
        }
        return GlobalResult.success(success);
    }

    @Override
    public GlobalResult<Boolean> updateUsername(CurrentUser currentUser, MemberUsernameRequest request) {
        if (currentUser == null) {
            return GlobalResult.error("未登录");
        }
        Long memberId = currentUser.getUserId();
        log.info("修改用户名请求: memberId={}, newUsername={}", memberId, request.newUsername());
        Boolean success = memberService.updateUsername(memberId, request.newUsername());
        if (Boolean.TRUE.equals(success)) {
            log.info("用户名修改成功: memberId={}, newUsername={}", memberId, request.newUsername());
        } else {
            log.warn("用户名修改失败: memberId={}", memberId);
        }
        return GlobalResult.success(success);
    }

    @Override
    public GlobalResult<Boolean> sendCode(SendCodeRequest request) {
        Boolean success;
        if (VerificationCodeType.SMS.matches(request.type())) {
            if (request.phone() == null || request.phone().trim().isEmpty()) {
                return GlobalResult.error("手机号不能为空");
            }
            success = verificationCodeService.sendSmsCode(request.phone());
        } else if (VerificationCodeType.EMAIL.matches(request.type())) {
            if (request.email() == null || request.email().trim().isEmpty()) {
                return GlobalResult.error("邮箱不能为空");
            }
            success = verificationCodeService.sendEmailCode(request.email());
        } else {
            return GlobalResult.error("不支持的验证码类型: " + request.type());
        }
        return GlobalResult.success(success);
    }

    @Override
    public GlobalResult<Boolean> resetPassword(ResetPasswordRequest request) {
        boolean isValid;
        if (VerificationCodeType.SMS.matches(request.type())) {
            if (request.account() == null || request.account().trim().isEmpty()) {
                return GlobalResult.error("手机号不能为空");
            }
            isValid = verificationCodeService.verifySmsCode(request.account(), request.code());
        } else if (VerificationCodeType.EMAIL.matches(request.type())) {
            if (request.account() == null || request.account().trim().isEmpty()) {
                return GlobalResult.error("邮箱不能为空");
            }
            isValid = verificationCodeService.verifyEmailCode(request.account(), request.code());
        } else {
            return GlobalResult.error("不支持的验证码类型: " + request.type());
        }

        if (!isValid) {
            return GlobalResult.error("验证码错误或已过期");
        }

        Boolean success = memberService.resetPassword(request.account(), request.newPassword(), request.code());
        log.info("密码重置成功: account={}", request.account());
        return GlobalResult.success(success);
    }

    @Override
    public GlobalResult<Boolean> verifyCode(VerifyCodeRequest request) {
        Boolean isValid;
        if (VerificationCodeType.SMS.matches(request.type())) {
            if (request.phone() == null || request.phone().trim().isEmpty()) {
                return GlobalResult.error("手机号不能为空");
            }
            isValid = verificationCodeService.verifySmsCode(request.phone(), request.code());
        } else if (VerificationCodeType.EMAIL.matches(request.type())) {
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

    private MemberLoginResponse buildLoginResponse(Member member, String extraClaimKey, Object extraClaimValue) {
        Map<String, Object> claims = buildBaseClaims(member);
        if (extraClaimKey != null && extraClaimValue != null) {
            claims.put(extraClaimKey, extraClaimValue);
        }
        String jwtToken = jwtTokenUtil.generateToken(resolveSubject(member), claims);
        String refreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(refreshToken, member.getId());
        return buildLoginResponse(member, jwtToken, refreshToken);
    }

    private MemberLoginResponse buildLoginResponse(Member member) {
        Map<String, Object> claims = buildBaseClaims(member);
        String jwtToken = jwtTokenUtil.generateToken(resolveSubject(member), claims);
        String refreshToken = Utils.genKey();
        authCacheService.storeMemberRefreshToken(refreshToken, member.getId());
        return buildLoginResponse(member, jwtToken, refreshToken);
    }

    private MemberLoginResponse buildLoginResponse(Member member, String jwtToken, String refreshToken) {
        return new MemberLoginResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getAvatarUrl(),
                jwtToken,
                refreshToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                MemberJwtConstants.ACCESS_TOKEN_EXPIRY_MS,
                Duration.ofHours(AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS).toMillis()
        );
    }

    private Map<String, Object> buildBaseClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(MemberJwtConstants.CLAIM_MEMBER_ID, member.getId());
        claims.put(MemberJwtConstants.CLAIM_TYPE, MemberJwtConstants.TYPE_MEMBER);
        return claims;
    }

    private String resolveSubject(Member member) {
        String username = member.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return member.getEmail() != null ? member.getEmail() : member.getPhone();
        }
        return username;
    }
}
