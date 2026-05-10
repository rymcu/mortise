package com.rymcu.mortise.member.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.cache.service.CacheService;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.member.api.model.DesktopAuthorizationCodePayload;
import com.rymcu.mortise.member.api.model.DesktopTokenRequest;
import com.rymcu.mortise.member.api.model.DesktopTokenResponse;
import com.rymcu.mortise.member.api.service.ApiMemberService;
import com.rymcu.mortise.member.api.service.DesktopOAuthService;
import com.rymcu.mortise.member.constant.DesktopOAuthConstants;
import com.rymcu.mortise.member.constant.MemberJwtConstants;
import com.rymcu.mortise.member.entity.Member;
import com.rymcu.mortise.member.entity.MemberClientSession;
import com.rymcu.mortise.member.service.MemberClientSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 桌面端授权桥服务实现。
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DesktopOAuthServiceImpl implements DesktopOAuthService {

    private static final String ERROR_INVALID_CLIENT = "invalid_client";
    private static final String ERROR_INVALID_REDIRECT_URI = "invalid_redirect_uri";
    private static final String ERROR_INVALID_STATE = "invalid_state";
    private static final String ERROR_INVALID_CODE = "invalid_code";
    private static final String ERROR_PKCE_FAILED = "pkce_verification_failed";
    private static final String ERROR_SESSION_REVOKED = "session_revoked";
    private static final String ERROR_AUTHORIZATION_EXPIRED = "authorization_expired";

    private final ApiMemberService memberService;
    private final MemberClientSessionService clientSessionService;
    private final AuthCacheService authCacheService;
    private final CacheService cacheService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;

    @Override
    public String authorize(CurrentUser currentUser,
                            String clientId,
                            String redirectUri,
                            String state,
                            String codeChallenge,
                            String codeChallengeMethod,
                            String scope,
                            String deviceName,
                            String deviceFingerprint) {
        if (currentUser == null || !currentUser.isAuthenticated()) {
            throw new BusinessException("unauthorized");
        }
        validateAuthorizeRequest(clientId, redirectUri, state, codeChallenge, codeChallengeMethod);

        String code = Utils.genKey();
        String normalizedScope = StringUtils.defaultIfBlank(scope, DesktopOAuthConstants.DEFAULT_SCOPE);
        String deviceFingerprintHash = hashNullable(deviceFingerprint);
        DesktopAuthorizationCodePayload payload = new DesktopAuthorizationCodePayload(
                currentUser.getUserId(),
                clientId,
                redirectUri,
                codeChallenge,
                normalizedScope,
                StringUtils.abbreviate(StringUtils.trimToNull(deviceName), 120),
                deviceFingerprintHash,
                LocalDateTime.now().plusMinutes(DesktopOAuthConstants.AUTHORIZATION_CODE_EXPIRE_MINUTES)
        );

        cacheService.set(
                DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE,
                hashToken(code),
                serializeAuthorizationCodePayload(payload),
                DesktopOAuthConstants.AUTHORIZATION_CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("创建桌面端授权码: memberId={}, clientId={}, redirectUri={}",
                currentUser.getUserId(), clientId, redirectUri);

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DesktopTokenResponse token(DesktopTokenRequest request) {
        if (request == null || StringUtils.isBlank(request.grantType())) {
            throw new BusinessException("unsupported_grant_type");
        }

        if (DesktopOAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE.equals(request.grantType())) {
            return exchangeAuthorizationCode(request);
        }

        if (DesktopOAuthConstants.GRANT_TYPE_REFRESH_TOKEN.equals(request.grantType())) {
            return refreshDesktopToken(request);
        }

        throw new BusinessException("unsupported_grant_type");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean logout(CurrentUser currentUser, String refreshToken, Long sessionId) {
        if (StringUtils.isNotBlank(refreshToken)) {
            String refreshTokenHash = hashToken(refreshToken);
            Long cachedSessionId = cacheService.get(DesktopOAuthConstants.REFRESH_SESSION_CACHE, refreshTokenHash, Long.class);
            if (cachedSessionId != null) {
                clientSessionService.revokeSession(cachedSessionId);
                cacheService.delete(DesktopOAuthConstants.REFRESH_SESSION_CACHE, refreshTokenHash);
                cacheService.delete(DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE, cachedSessionId.toString());
            }
            authCacheService.removeMemberRefreshToken(refreshTokenHash);
        }

        if (sessionId != null && currentUser != null) {
            MemberClientSession session = clientSessionService.getById(sessionId);
            if (session != null
                    && session.getMemberId().equals(currentUser.getUserId())) {
                Boolean revoked = clientSessionService.revokeSession(sessionId);
                if (Boolean.TRUE.equals(revoked)) {
                    revokeRefreshTokenBySessionId(sessionId);
                }
                return revoked;
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeSession(Long memberId, Long sessionId) {
        if (memberId == null || sessionId == null) {
            return false;
        }

        Boolean revoked = clientSessionService.revokeMemberSession(memberId, sessionId);
        if (!Boolean.TRUE.equals(revoked)) {
            return false;
        }

        revokeRefreshTokenBySessionId(sessionId);
        return true;
    }

    private DesktopTokenResponse exchangeAuthorizationCode(DesktopTokenRequest request) {
        if (StringUtils.isBlank(request.code()) || StringUtils.isBlank(request.redirectUri())
                || StringUtils.isBlank(request.codeVerifier())) {
            throw new BusinessException(ERROR_INVALID_CODE);
        }

        String codeKey = hashToken(request.code());
        String payloadJson = cacheService.get(
                DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE,
                codeKey,
                String.class
        );
        cacheService.delete(DesktopOAuthConstants.AUTHORIZATION_CODE_CACHE, codeKey);

        DesktopAuthorizationCodePayload payload = deserializeAuthorizationCodePayload(payloadJson);
        if (payload == null) {
            throw new BusinessException(ERROR_INVALID_CODE);
        }
        if (payload.expiresAt() == null || payload.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ERROR_AUTHORIZATION_EXPIRED);
        }
        if (!payload.redirectUri().equals(request.redirectUri())) {
            throw new BusinessException(ERROR_INVALID_REDIRECT_URI);
        }
        if (!verifyPkce(request.codeVerifier(), payload.codeChallenge())) {
            throw new BusinessException(ERROR_PKCE_FAILED);
        }

        Member member = getActiveMember(payload.memberId());
        MemberClientSession session = clientSessionService.createSession(
                member.getId(),
                payload.clientId(),
                payload.deviceName(),
                payload.deviceFingerprintHash(),
                payload.scope()
        );

        return buildTokenResponse(member, session, payload.scope(), null);
    }

    private DesktopTokenResponse refreshDesktopToken(DesktopTokenRequest request) {
        if (StringUtils.isBlank(request.refreshToken())) {
            throw new BusinessException(ERROR_SESSION_REVOKED);
        }

        String refreshTokenHash = hashToken(request.refreshToken());
        Long memberId = authCacheService.getMemberIdByRefreshToken(refreshTokenHash);
        Long sessionId = cacheService.get(DesktopOAuthConstants.REFRESH_SESSION_CACHE, refreshTokenHash, Long.class);
        if (memberId == null || sessionId == null) {
            throw new BusinessException(ERROR_SESSION_REVOKED);
        }

        MemberClientSession session = clientSessionService.touchActiveSession(sessionId);
        if (session == null || !memberId.equals(session.getMemberId())) {
            authCacheService.removeMemberRefreshToken(refreshTokenHash);
            cacheService.delete(DesktopOAuthConstants.REFRESH_SESSION_CACHE, refreshTokenHash);
            cacheService.delete(DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE, sessionId.toString());
            throw new BusinessException(ERROR_SESSION_REVOKED);
        }

        Member member = getActiveMember(memberId);
        return buildTokenResponse(member, session, session.getScope(), request.refreshToken());
    }

    private DesktopTokenResponse buildTokenResponse(Member member,
                                                    MemberClientSession session,
                                                    String scope,
                                                    String oldRefreshToken) {
        if (StringUtils.isNotBlank(oldRefreshToken)) {
            authCacheService.removeMemberRefreshToken(hashToken(oldRefreshToken));
            cacheService.delete(DesktopOAuthConstants.REFRESH_SESSION_CACHE, hashToken(oldRefreshToken));
            cacheService.delete(DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE, session.getId().toString());
        }

        Map<String, Object> claims = buildClaims(member, session, scope);
        String jwtToken = jwtTokenUtil.generateToken(resolveSubject(member), claims);
        String refreshToken = Utils.genKey();
        String refreshTokenHash = hashToken(refreshToken);
        authCacheService.storeMemberRefreshToken(refreshTokenHash, member.getId());
        cacheService.set(
                DesktopOAuthConstants.REFRESH_SESSION_CACHE,
                refreshTokenHash,
                session.getId(),
                AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS,
                TimeUnit.HOURS
        );
        cacheService.set(
                DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE,
                session.getId().toString(),
                refreshTokenHash,
                AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        return new DesktopTokenResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getAvatarUrl(),
                jwtToken,
                refreshToken,
                jwtTokenUtil.getTokenPrefix().trim(),
                MemberJwtConstants.ACCESS_TOKEN_EXPIRY_MS,
                Duration.ofHours(AuthCacheConstant.MEMBER_REFRESH_TOKEN_EXPIRE_HOURS).toMillis(),
                session.getId(),
                session.getClientId()
        );
    }

    private void revokeRefreshTokenBySessionId(Long sessionId) {
        String refreshTokenHash = cacheService.get(
                DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE,
                sessionId.toString(),
                String.class
        );
        if (StringUtils.isBlank(refreshTokenHash)) {
            return;
        }

        authCacheService.removeMemberRefreshToken(refreshTokenHash);
        cacheService.delete(DesktopOAuthConstants.REFRESH_SESSION_CACHE, refreshTokenHash);
        cacheService.delete(DesktopOAuthConstants.SESSION_REFRESH_INDEX_CACHE, sessionId.toString());
    }

    private Map<String, Object> buildClaims(Member member, MemberClientSession session, String scope) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(MemberJwtConstants.CLAIM_MEMBER_ID, member.getId());
        claims.put(MemberJwtConstants.CLAIM_TYPE, MemberJwtConstants.TYPE_MEMBER);
        claims.put(MemberJwtConstants.CLAIM_LOGIN_TYPE, MemberJwtConstants.LOGIN_TYPE_DESKTOP);
        claims.put(MemberJwtConstants.CLAIM_SESSION_ID, session.getId());
        claims.put(MemberJwtConstants.CLAIM_CLIENT_ID, session.getClientId());
        claims.put(MemberJwtConstants.CLAIM_LOGIN_CHANNEL, MemberJwtConstants.LOGIN_CHANNEL_DESKTOP_BROWSER);
        claims.put(MemberJwtConstants.CLAIM_SCOPE, StringUtils.defaultIfBlank(scope, DesktopOAuthConstants.DEFAULT_SCOPE));
        return claims;
    }

    private Member getActiveMember(Long memberId) {
        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            throw new BusinessException("member_not_found");
        }
        member.setPasswordHash(null);
        return member;
    }

    private void validateAuthorizeRequest(String clientId,
                                          String redirectUri,
                                          String state,
                                          String codeChallenge,
                                          String codeChallengeMethod) {
        if (!DesktopOAuthConstants.RODAK_CLIENT_ID.equals(clientId)) {
            throw new BusinessException(ERROR_INVALID_CLIENT);
        }
        if (!DesktopOAuthConstants.CODE_CHALLENGE_METHOD_S256.equals(codeChallengeMethod)) {
            throw new BusinessException(ERROR_PKCE_FAILED);
        }
        if (StringUtils.isBlank(state)) {
            throw new BusinessException(ERROR_INVALID_STATE);
        }
        if (StringUtils.isBlank(codeChallenge)) {
            throw new BusinessException(ERROR_PKCE_FAILED);
        }
        validateLoopbackRedirectUri(redirectUri);
    }

    private void validateLoopbackRedirectUri(String redirectUri) {
        try {
            URI uri = URI.create(redirectUri);
            if (!"http".equalsIgnoreCase(uri.getScheme())
                    || !"127.0.0.1".equals(uri.getHost())
                    || uri.getPort() <= 0
                    || !"/auth/callback".equals(uri.getPath())) {
                throw new BusinessException(ERROR_INVALID_REDIRECT_URI);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ERROR_INVALID_REDIRECT_URI, e);
        }
    }

    private boolean verifyPkce(String codeVerifier, String expectedChallenge) {
        return expectedChallenge.equals(base64UrlSha256(codeVerifier));
    }

    private String serializeAuthorizationCodePayload(DesktopAuthorizationCodePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BusinessException("authorization_code_serialization_failed", e);
        }
    }

    private DesktopAuthorizationCodePayload deserializeAuthorizationCodePayload(String payloadJson) {
        if (StringUtils.isBlank(payloadJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(payloadJson, DesktopAuthorizationCodePayload.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ERROR_INVALID_CODE, e);
        }
    }

    private String base64UrlSha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String hashNullable(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return hashToken(value);
    }

    private String hashToken(String value) {
        return base64UrlSha256(value);
    }

    private String resolveSubject(Member member) {
        if (StringUtils.isNotBlank(member.getUsername())) {
            return member.getUsername();
        }
        return StringUtils.defaultIfBlank(member.getEmail(), member.getPhone());
    }
}
