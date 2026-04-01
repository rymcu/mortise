package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.controller.facade.AuthAdminFacade;
import com.rymcu.mortise.system.controller.vo.UserSessionVO;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.model.AuthInfo;
import com.rymcu.mortise.system.model.ForgetPasswordInfo;
import com.rymcu.mortise.system.model.RefreshTokenInfo;
import com.rymcu.mortise.system.model.TokenUser;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.model.auth.EmailUpdateCodeRequest;
import com.rymcu.mortise.system.model.auth.EmailUpdateConfirmInfo;
import com.rymcu.mortise.system.model.auth.LoginInfo;
import com.rymcu.mortise.system.model.auth.OAuth2ProviderInfo;
import com.rymcu.mortise.system.model.auth.RegisterInfo;
import com.rymcu.mortise.system.query.UserQueryService;
import com.rymcu.mortise.system.service.AuthService;
import com.rymcu.mortise.system.service.command.UserCommandService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@Slf4j
@Component
public class AuthAdminFacadeImpl implements AuthAdminFacade {

    private final AuthService authService;
    private final TokenManager tokenManager;
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final Oauth2ClientConfigService oauth2ClientConfigService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthAdminFacadeImpl(AuthService authService,
                               TokenManager tokenManager,
                               UserQueryService userQueryService,
                               UserCommandService userCommandService,
                               Oauth2ClientConfigService oauth2ClientConfigService,
                               JwtTokenUtil jwtTokenUtil) {
        this.authService = authService;
        this.tokenManager = tokenManager;
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
        this.oauth2ClientConfigService = oauth2ClientConfigService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public GlobalResult<List<OAuth2ProviderInfo>> listOAuth2Providers(String appType) {
        List<OAuth2ProviderInfo> providers = oauth2ClientConfigService
                .loadOauth2ClientConfigAllEnabledByAppType(appType)
                .stream()
                .map(c -> new OAuth2ProviderInfo(c.getRegistrationId(), c.getClientName(), c.getIcon()))
                .toList();
        return GlobalResult.success(providers);
    }

    @Override
    public GlobalResult<TokenUser> login(LoginInfo loginInfo) {
        log.info("用户登录请求: {}", loginInfo.getAccount());
        return GlobalResult.success(authService.login(loginInfo.getAccount(), loginInfo.getPassword()));
    }

    @Override
    public GlobalResult<Boolean> register(RegisterInfo registerInfo) throws AccountExistsException {
        log.info("用户注册请求: {}", registerInfo.getEmail());
        Boolean result = authService.register(
                registerInfo.getEmail(),
                registerInfo.getNickname(),
                registerInfo.getPassword(),
                registerInfo.getCode()
        );
        return GlobalResult.success(result);
    }

    @Override
    public GlobalResult<TokenUser> refreshToken(RefreshTokenInfo refreshTokenInfo) {
        log.info("Token 刷新请求");
        return GlobalResult.success(authService.refreshToken(refreshTokenInfo.getRefreshToken()));
    }

    @Override
    public GlobalResult<?> logout(CurrentUser currentUser, String authHeader) {
        String account = currentUser.getUsername();
        if (account != null) {
            String token = null;
            String tokenPrefix = jwtTokenUtil.getTokenPrefix();
            if (authHeader != null && tokenPrefix != null && authHeader.startsWith(tokenPrefix)) {
                token = authHeader.substring(tokenPrefix.length());
            }

            if (token != null) {
                tokenManager.revokeToken(account, token);
                log.info("用户登出成功（Token 已加入黑名单）: {}", account);
            } else {
                tokenManager.deleteToken(account);
                log.info("用户登出成功: {}", account);
            }
        }
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<UserSessionVO> getUserSession(CurrentUser currentUser) {
        log.info("获取用户会话信息: {}", currentUser.getUsername());
        AuthInfo authInfo = authService.userSession(currentUser.getUserId());
        return GlobalResult.success(new UserSessionVO(authInfo));
    }

    @Override
    public GlobalResult<List<Link>> menus(CurrentUser currentUser) {
        return GlobalResult.success(authService.userMenus(currentUser.getUserId()));
    }

    @Override
    public GlobalResult<String> requestPasswordReset(String email) throws MessagingException, AccountNotFoundException {
        authService.requestPasswordReset(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    @Override
    public GlobalResult<Boolean> resetPassword(ForgetPasswordInfo forgetPassword) {
        return GlobalResult.success(authService.forgetPassword(forgetPassword.getCode(), forgetPassword.getPassword()));
    }

    @Override
    public GlobalResult<String> requestEmailVerify(String email) throws MessagingException, AccountExistsException {
        authService.requestEmailVerify(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    @Override
    public GlobalResult<TokenUser> oauth2Login(String state) {
        return GlobalResult.success(authService.getOauth2TokenUser(state));
    }

    @Override
    public GlobalResult<UserProfileInfo> getProfile(CurrentUser currentUser) {
        return GlobalResult.success(userQueryService.getUserProfileInfo(currentUser.getUserId()));
    }

    @Override
    public GlobalResult<Boolean> updateProfile(CurrentUser currentUser, UserProfileInfo userProfileInfo) {
        log.info("更新用户资料请求: {}", currentUser.getUsername());
        return GlobalResult.success(userCommandService.updateUserProfileInfo(userProfileInfo, currentUser.getUserId()));
    }

    @Override
    public GlobalResult<String> sendEmailUpdateCode(CurrentUser currentUser, EmailUpdateCodeRequest request) {
        log.info("发送邮箱更换验证码请求: userId={}", currentUser.getUserId());
        userCommandService.sendEmailUpdateCode(currentUser.getUserId(), request.newEmail());
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    @Override
    public GlobalResult<Boolean> confirmEmailUpdate(CurrentUser currentUser, EmailUpdateConfirmInfo confirmInfo) {
        log.info("确认邮箱更换请求: userId={}", currentUser.getUserId());
        return GlobalResult.success(
                userCommandService.confirmEmailUpdate(currentUser.getUserId(), confirmInfo.newEmail(), confirmInfo.code())
        );
    }
}
