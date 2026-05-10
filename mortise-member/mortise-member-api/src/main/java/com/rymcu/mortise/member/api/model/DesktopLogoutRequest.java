package com.rymcu.mortise.member.api.model;

/**
 * 桌面端退出请求。
 *
 * @param refreshToken 刷新令牌
 * @param sessionId    客户端会话ID
 */
public record DesktopLogoutRequest(
        String refreshToken,
        Long sessionId
) {
}
