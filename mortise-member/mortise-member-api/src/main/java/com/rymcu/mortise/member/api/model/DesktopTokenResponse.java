package com.rymcu.mortise.member.api.model;

/**
 * 桌面端 Token 响应。
 *
 * @param memberId             会员ID
 * @param username             用户名
 * @param nickname             昵称
 * @param avatarUrl            头像
 * @param token                访问令牌
 * @param refreshToken         刷新令牌
 * @param tokenType            Token 类型
 * @param accessTokenExpiryMs  访问令牌有效期
 * @param refreshTokenExpiryMs 刷新令牌有效期
 * @param sessionId            客户端会话ID
 * @param clientId             客户端ID
 */
public record DesktopTokenResponse(
        Long memberId,
        String username,
        String nickname,
        String avatarUrl,
        String token,
        String refreshToken,
        String tokenType,
        Long accessTokenExpiryMs,
        Long refreshTokenExpiryMs,
        Long sessionId,
        String clientId
) {
}
