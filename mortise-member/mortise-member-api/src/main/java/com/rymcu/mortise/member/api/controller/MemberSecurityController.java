package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.member.api.model.MemberClientSessionResponse;
import com.rymcu.mortise.member.api.service.DesktopOAuthService;
import com.rymcu.mortise.member.constant.DesktopOAuthConstants;
import com.rymcu.mortise.member.constant.MemberJwtConstants;
import com.rymcu.mortise.member.entity.MemberClientSession;
import com.rymcu.mortise.member.service.MemberClientSessionService;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

/**
 * 会员账号安全中心 Controller。
 *
 * @author ronger
 */
@ApiController
@RequestMapping("/app/security")
@RequiredArgsConstructor
@Tag(name = "会员账号安全", description = "会员设备与会话安全管理接口")
public class MemberSecurityController {

    private final MemberClientSessionService clientSessionService;
    private final DesktopOAuthService desktopOAuthService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/client-sessions")
    @Operation(summary = "查询桌面客户端会话")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询会员客户端会话")
    public GlobalResult<List<MemberClientSessionResponse>> listClientSessions(
            @AuthenticationPrincipal CurrentUser currentUser,
            HttpServletRequest request) {
        Long memberId = requireMemberId(currentUser);
        Long currentSessionId = resolveCurrentSessionId(request);
        List<MemberClientSessionResponse> sessions = clientSessionService.listMemberSessions(memberId)
                .stream()
                .map(session -> toResponse(session, currentSessionId))
                .toList();
        return GlobalResult.success(sessions);
    }

    @DeleteMapping("/client-sessions/{sessionId}")
    @Operation(summary = "撤销桌面客户端会话")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "撤销会员客户端会话")
    public GlobalResult<Boolean> revokeClientSession(@AuthenticationPrincipal CurrentUser currentUser,
                                                     @PathVariable Long sessionId) {
        Long memberId = requireMemberId(currentUser);
        Boolean revoked = desktopOAuthService.revokeSession(memberId, sessionId);
        if (!Boolean.TRUE.equals(revoked)) {
            throw new BusinessException("session_not_found");
        }
        return GlobalResult.success(true);
    }

    private Long requireMemberId(CurrentUser currentUser) {
        if (currentUser == null || !currentUser.isAuthenticated() || currentUser.getUserId() == null) {
            throw new BusinessException("unauthorized");
        }
        return currentUser.getUserId();
    }

    private MemberClientSessionResponse toResponse(MemberClientSession session, Long currentSessionId) {
        return new MemberClientSessionResponse(
                session.getId(),
                session.getClientId(),
                resolveClientName(session.getClientId()),
                StringUtils.defaultIfBlank(session.getDeviceName(), "未知设备"),
                session.getStatus(),
                session.getLastActiveAt(),
                session.getRevokedAt(),
                session.getCreatedTime(),
                Objects.equals(session.getId(), currentSessionId)
        );
    }

    private String resolveClientName(String clientId) {
        if (DesktopOAuthConstants.RODAK_CLIENT_ID.equals(clientId)) {
            return "Rodak";
        }
        return StringUtils.defaultIfBlank(clientId, "未知客户端");
    }

    private Long resolveCurrentSessionId(HttpServletRequest request) {
        String token = extractToken(request);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Object sessionId = jwtTokenUtil.getClaimFromToken(
                token,
                claims -> claims.get(MemberJwtConstants.CLAIM_SESSION_ID)
        );
        if (sessionId instanceof Number number) {
            return number.longValue();
        }
        if (sessionId instanceof String value && StringUtils.isNumeric(value)) {
            return Long.valueOf(value);
        }
        return null;
    }

    private String extractToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtTokenUtil.getTokenHeader());
        if (StringUtils.isBlank(headerValue)) {
            return null;
        }

        String token = headerValue.trim();
        String prefix = StringUtils.defaultIfBlank(StringUtils.trimToNull(jwtTokenUtil.getTokenPrefix()), "Bearer");
        while (token.length() > prefix.length()
                && token.regionMatches(true, 0, prefix, 0, prefix.length())) {
            token = token.substring(prefix.length()).trim();
        }
        return StringUtils.trimToNull(token);
    }
}
