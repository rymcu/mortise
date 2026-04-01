package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.model.ForgetPasswordInfo;
import com.rymcu.mortise.system.model.RefreshTokenInfo;
import com.rymcu.mortise.system.model.TokenUser;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.controller.vo.UserSessionVO;
import com.rymcu.mortise.system.model.auth.EmailUpdateCodeRequest;
import com.rymcu.mortise.system.model.auth.EmailUpdateConfirmInfo;
import com.rymcu.mortise.system.model.auth.LoginInfo;
import com.rymcu.mortise.system.model.auth.OAuth2ProviderInfo;
import com.rymcu.mortise.system.model.auth.RegisterInfo;
import jakarta.mail.MessagingException;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

public interface AuthAdminFacade {

    GlobalResult<List<OAuth2ProviderInfo>> listOAuth2Providers(String appType);

    GlobalResult<TokenUser> login(LoginInfo loginInfo);

    GlobalResult<Boolean> register(RegisterInfo registerInfo) throws AccountExistsException;

    GlobalResult<TokenUser> refreshToken(RefreshTokenInfo refreshTokenInfo);

    GlobalResult<?> logout(CurrentUser currentUser, String authHeader);

    GlobalResult<UserSessionVO> getUserSession(CurrentUser currentUser);

    GlobalResult<List<Link>> menus(CurrentUser currentUser);

    GlobalResult<String> requestPasswordReset(String email) throws MessagingException, AccountNotFoundException;

    GlobalResult<Boolean> resetPassword(ForgetPasswordInfo forgetPassword);

    GlobalResult<String> requestEmailVerify(String email) throws MessagingException, AccountExistsException;

    GlobalResult<TokenUser> oauth2Login(String state);

    GlobalResult<UserProfileInfo> getProfile(CurrentUser currentUser);

    GlobalResult<Boolean> updateProfile(CurrentUser currentUser, UserProfileInfo userProfileInfo);

    GlobalResult<String> sendEmailUpdateCode(CurrentUser currentUser, EmailUpdateCodeRequest request);

    GlobalResult<Boolean> confirmEmailUpdate(CurrentUser currentUser, EmailUpdateConfirmInfo confirmInfo);
}
