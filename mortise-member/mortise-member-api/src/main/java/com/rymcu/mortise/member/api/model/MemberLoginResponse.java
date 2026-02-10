package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会员登录响应
 *
 * @author ronger
 */
@Schema(description = "会员登录响应")
public record MemberLoginResponse(
        @Schema(description = "会员ID")
        Long memberId,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "昵称")
        String nickname,
        @Schema(description = "头像URL")
        String avatarUrl,
        @Schema(description = "访问令牌")
        String token,
        @Schema(description = "刷新令牌")
        String refreshToken,
        @Schema(description = "令牌类型", example = "Bearer")
        String tokenType,
        @Schema(description = "访问令牌过期时间（毫秒）", example = "1800000")
        Long expiresIn,
        @Schema(description = "刷新令牌过期时间（毫秒）", example = "86400000")
        Long refreshExpiresIn
) {
}
