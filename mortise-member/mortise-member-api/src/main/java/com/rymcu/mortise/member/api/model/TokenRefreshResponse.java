package com.rymcu.mortise.member.api.model;

/**
 * Token 刷新响应
 *
 * @author ronger
 */
public record TokenRefreshResponse(
        String token,
        String tokenType,
        Long expiresIn
) {
}
