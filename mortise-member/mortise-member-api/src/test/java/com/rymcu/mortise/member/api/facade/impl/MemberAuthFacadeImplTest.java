package com.rymcu.mortise.member.api.facade.impl;

import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.member.api.model.MemberLoginRequest;
import com.rymcu.mortise.member.api.model.MemberLoginResponse;
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.api.service.VerificationCodeService;
import com.rymcu.mortise.member.entity.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberAuthFacadeImplTest {

    @Test
    void loginShouldReturnJwtAndRefreshToken() {
        ApiMemberService memberService = mock(ApiMemberService.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);
        AuthCacheService authCacheService = mock(AuthCacheService.class);

        Member member = new Member();
        member.setId(5L);
        member.setUsername("ronger");
        member.setNickname("ronger");
        member.setAvatarUrl("https://static.rymcu.com/avatar.png");

        when(memberService.login("ronger-x@outlook.com", "secret")).thenReturn(member);
        when(jwtTokenUtil.generateToken(eq("ronger"), anyMap())).thenReturn("jwt-token");
        when(jwtTokenUtil.getTokenPrefix()).thenReturn("Bearer ");

        MemberAuthFacadeImpl facade = new MemberAuthFacadeImpl(
                memberService,
                jwtTokenUtil,
                verificationCodeService,
                authCacheService
        );

        GlobalResult<MemberLoginResponse> result = facade.login(new MemberLoginRequest("ronger-x@outlook.com", "secret"));

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt-token", result.getData().token());
        assertNotNull(result.getData().refreshToken());
        verify(authCacheService).storeMemberRefreshToken(result.getData().refreshToken(), 5L);
    }
}
