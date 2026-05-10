package com.rymcu.mortise.member.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.member.api.model.DesktopAuthorizationCodePayload;
import com.rymcu.mortise.member.api.model.DesktopTokenRequest;
import com.rymcu.mortise.member.api.model.DesktopTokenResponse;
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.constant.DesktopOAuthConstants;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.entity.MemberClientSession;
import com.rymcu.mortise.member.service.MemberClientSessionService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DesktopOAuthServiceImplTest {

    @Test
    void authorizeShouldCreateOneTimeCodeAndReturnLoopbackRedirect() {
        TestFixture fixture = new TestFixture();
        CurrentUser currentUser = currentUser(5L);

        String redirect = fixture.service.authorize(
                currentUser,
                DesktopOAuthConstants.RODAK_CLIENT_ID,
                "http://127.0.0.1:49152/auth/callback",
                "state-1",
                "challenge",
                DesktopOAuthConstants.CODE_CHALLENGE_METHOD_S256,
                "profile",
                "Dev PC",
                "fingerprint"
        );

        assertEquals(true, redirect.startsWith("http://127.0.0.1:49152/auth/callback?code="));
        assertEquals(true, redirect.contains("&state=state-1"));
        verify(fixture.cacheService).set(
                eq(DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE),
                any(String.class),
                any(String.class),
                eq(DesktopOAuthConstants.AUTHORIZATION_CODE_EXPIRE_MINUTES),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void authorizeShouldRejectNonLoopbackRedirectUri() {
        TestFixture fixture = new TestFixture();

        BusinessException error = assertThrows(BusinessException.class, () -> fixture.service.authorize(
                currentUser(5L),
                DesktopOAuthConstants.RODAK_CLIENT_ID,
                "https://example.com/auth/callback",
                "state-1",
                "challenge",
                DesktopOAuthConstants.CODE_CHALLENGE_METHOD_S256,
                "profile",
                "Dev PC",
                "fingerprint"
        ));

        assertEquals("invalid_redirect_uri", error.getMessage());
    }

    @Test
    void tokenShouldRejectWrongPkceVerifierAndDeleteCode() {
        TestFixture fixture = new TestFixture();
        String code = "code-1";
        when(fixture.cacheService.get(
                eq(DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE),
                any(String.class),
                eq(String.class)
        )).thenReturn(payloadJson(new DesktopAuthorizationCodePayload(
                5L,
                DesktopOAuthConstants.RODAK_CLIENT_ID,
                "http://127.0.0.1:49152/auth/callback",
                challengeOf("right-verifier"),
                "profile",
                "Dev PC",
                "device-hash",
                LocalDateTime.now().plusMinutes(5)
        )));

        BusinessException error = assertThrows(BusinessException.class, () -> fixture.service.token(
                new DesktopTokenRequest(
                        DesktopOAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE,
                        code,
                        "http://127.0.0.1:49152/auth/callback",
                        "wrong-verifier",
                        null
                )
        ));

        assertEquals("pkce_verification_failed", error.getMessage());
        verify(fixture.cacheService).delete(eq(DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE), any(String.class));
    }

    @Test
    void tokenShouldExchangeAuthorizationCodeAndCreateClientSession() {
        TestFixture fixture = new TestFixture();
        Member member = member();
        MemberClientSession session = session();
        when(fixture.cacheService.get(
                eq(DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE),
                any(String.class),
                eq(String.class)
        )).thenReturn(payloadJson(new DesktopAuthorizationCodePayload(
                5L,
                DesktopOAuthConstants.RODAK_CLIENT_ID,
                "http://127.0.0.1:49152/auth/callback",
                challengeOf("verifier"),
                "profile",
                "Dev PC",
                "device-hash",
                LocalDateTime.now().plusMinutes(5)
        )));
        when(fixture.memberService.getMemberById(5L)).thenReturn(member);
        when(fixture.clientSessionService.createSession(5L, DesktopOAuthConstants.RODAK_CLIENT_ID, "Dev PC", "device-hash"))
                .thenReturn(session);
        when(fixture.jwtTokenUtil.generateToken(eq("ronger"), anyMap())).thenReturn("jwt-token");
        when(fixture.jwtTokenUtil.getTokenPrefix()).thenReturn("Bearer ");

        DesktopTokenResponse response = fixture.service.token(new DesktopTokenRequest(
                DesktopOAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE,
                "code-1",
                "http://127.0.0.1:49152/auth/callback",
                "verifier",
                null
        ));

        assertNotNull(response.refreshToken());
        assertEquals("jwt-token", response.token());
        assertEquals(99L, response.sessionId());
        verify(fixture.authCacheService).storeMemberRefreshToken(any(String.class), eq(5L));
        verify(fixture.cacheService).set(
                eq(DesktopOAuthConstants.REFRESH_SESSION_CACHE),
                any(String.class),
                eq(99L),
                eq(AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void refreshShouldRotateRefreshTokenAndTouchSession() {
        TestFixture fixture = new TestFixture();
        Member member = member();
        MemberClientSession session = session();
        when(fixture.authCacheService.getMemberIdByRefreshToken(any(String.class))).thenReturn(5L);
        when(fixture.cacheService.get(eq(DesktopOAuthConstants.REFRESH_SESSION_CACHE), any(String.class), eq(Long.class)))
                .thenReturn(99L);
        when(fixture.clientSessionService.touchActiveSession(99L)).thenReturn(session);
        when(fixture.memberService.getMemberById(5L)).thenReturn(member);
        when(fixture.jwtTokenUtil.generateToken(eq("ronger"), anyMap())).thenReturn("new-jwt");
        when(fixture.jwtTokenUtil.getTokenPrefix()).thenReturn("Bearer ");

        DesktopTokenResponse response = fixture.service.token(new DesktopTokenRequest(
                DesktopOAuthConstants.GRANT_TYPE_REFRESH_TOKEN,
                null,
                null,
                null,
                "old-refresh"
        ));

        assertEquals("new-jwt", response.token());
        verify(fixture.authCacheService).removeMemberRefreshToken(any(String.class));
        verify(fixture.authCacheService).storeMemberRefreshToken(any(String.class), eq(5L));
    }

    @Test
    void logoutShouldNotRevokeSessionIdWithoutCurrentUser() {
        TestFixture fixture = new TestFixture();
        when(fixture.clientSessionService.getById(99L)).thenReturn(session());

        Boolean result = fixture.service.logout(null, null, 99L);

        assertEquals(true, result);
        verify(fixture.clientSessionService, never()).revokeSession(99L);
    }

    @Test
    void logoutShouldRevokeSessionByRefreshTokenWithoutCurrentUser() {
        TestFixture fixture = new TestFixture();
        when(fixture.cacheService.get(eq(DesktopOAuthConstants.REFRESH_SESSION_CACHE), any(String.class), eq(Long.class)))
                .thenReturn(99L);

        Boolean result = fixture.service.logout(null, "refresh-token", null);

        assertEquals(true, result);
        verify(fixture.clientSessionService).revokeSession(99L);
        verify(fixture.authCacheService).removeMemberRefreshToken(any(String.class));
    }

    @Test
    void revokeSessionShouldRemoveIndexedRefreshToken() {
        TestFixture fixture = new TestFixture();
        when(fixture.clientSessionService.revokeMemberSession(5L, 99L)).thenReturn(true);
        when(fixture.cacheService.get(DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE, "99", String.class))
                .thenReturn("refresh-hash");

        Boolean result = fixture.service.revokeSession(5L, 99L);

        assertEquals(true, result);
        verify(fixture.authCacheService).removeMemberRefreshToken("refresh-hash");
        verify(fixture.cacheService).delete(DesktopOAuthConstants.REFRESH_SESSION_CACHE, "refresh-hash");
        verify(fixture.cacheService).delete(DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE, "99");
    }

    private static CurrentUser currentUser(Long userId) {
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.getUserId()).thenReturn(userId);
        when(currentUser.isAuthenticated()).thenReturn(true);
        return currentUser;
    }

    private static Member member() {
        Member member = new Member();
        member.setId(5L);
        member.setUsername("ronger");
        member.setNickname("Ronger");
        member.setAvatarUrl("https://static.rymcu.com/avatar.png");
        return member;
    }

    private static MemberClientSession session() {
        MemberClientSession session = new MemberClientSession();
        session.setId(99L);
        session.setMemberId(5L);
        session.setClientId(DesktopOAuthConstants.RODAK_CLIENT_ID);
        session.setStatus(DesktopOAuthConstants.SESSION_STATUS_ACTIVE);
        return session;
    }

    private static String challengeOf(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String payloadJson(DesktopAuthorizationCodePayload payload) {
        try {
            return objectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private static final class TestFixture {
        private final ApiMemberService memberService = mock(ApiMemberService.class);
        private final MemberClientSessionService clientSessionService = mock(MemberClientSessionService.class);
        private final AuthCacheService authCacheService = mock(AuthCacheService.class);
        private final CacheService cacheService = mock(CacheService.class);
        private final JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        private final DesktopOAuthServiceImpl service = new DesktopOAuthServiceImpl(
                memberService,
                clientSessionService,
                authCacheService,
                cacheService,
                jwtTokenUtil,
                objectMapper()
        );
    }
}
