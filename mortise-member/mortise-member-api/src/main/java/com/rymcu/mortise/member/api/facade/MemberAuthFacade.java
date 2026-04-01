package com.rymcu.mortise.member.api.facade;

import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
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

public interface MemberAuthFacade {

    GlobalResult<Long> register(MemberRegisterRequest request);

    GlobalResult<MemberLoginResponse> login(MemberLoginRequest request);

    GlobalResult<MemberLoginResponse> loginByPhone(PhoneLoginRequest request);

    GlobalResult<MemberLoginResponse> refreshToken(RefreshTokenRequest request);

    GlobalResult<TokenRefreshResponse> refreshTokenByJwt(String authHeader);

    GlobalResult<Member> getProfile(CurrentUser currentUser);

    GlobalResult<Boolean> updateProfile(CurrentUser currentUser, MemberUpdateRequest request);

    GlobalResult<Boolean> updatePassword(CurrentUser currentUser, MemberPasswordRequest request);

    GlobalResult<Boolean> updateUsername(CurrentUser currentUser, MemberUsernameRequest request);

    GlobalResult<Boolean> sendCode(SendCodeRequest request);

    GlobalResult<Boolean> resetPassword(ResetPasswordRequest request);

    GlobalResult<Boolean> verifyCode(VerifyCodeRequest request);
}
