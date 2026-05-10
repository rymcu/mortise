package com.rymcu.mortise.member.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌面端授权码缓存载荷。
 */
public record DesktopAuthorizationCodePayload(
        Long memberId,
        String clientId,
        String redirectUri,
        String codeChallenge,
        String scope,
        String deviceName,
        String deviceFingerprintHash,
        LocalDateTime expiresAt
) implements Serializable {
}
