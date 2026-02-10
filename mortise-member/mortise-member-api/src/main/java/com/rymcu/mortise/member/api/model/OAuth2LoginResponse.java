package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OAuth2 登录响应
 *
 * @author ronger
 */
@Schema(description = "OAuth2 登录响应")
public record OAuth2LoginResponse(
        @Schema(description = "会员ID", example = "123456")
        Long memberId,
        @Schema(description = "会员用户名", example = "john_doe")
        String username,
        @Schema(description = "会员昵称", example = "John")
        String nickname,
        @Schema(description = "会员头像URL", example = "https://example.com/avatar.jpg")
        String avatarUrl,
        @Schema(description = "JWT 访问令牌")
        String token,
        @Schema(description = "刷新令牌（用于获取新的访问令牌）")
        String refreshToken,
        @Schema(description = "令牌类型", example = "Bearer")
        String tokenType,
        @Schema(description = "令牌过期时间（毫秒）", example = "1800000")
        Long expiresIn,
        @Schema(description = "刷新令牌过期时间（毫秒）", example = "86400000")
        Long refreshExpiresIn,
        @Schema(description = "第三方提供商用户ID（openid）", example = "OPENID123456")
        String openId,
        @Schema(description = "第三方提供商统一ID（unionid）", example = "UNIONID123456")
        String unionId,
        @Schema(description = "第三方提供商昵称", example = "WeChat_User")
        String thirdPartyNickname,
        @Schema(description = "第三方提供商头像", example = "https://weixin.qq.com/avatar.jpg")
        String thirdPartyAvatarUrl,
        @Schema(description = "是否为新用户", example = "false")
        Boolean isNewUser,
        @Schema(description = "绑定状态（true: 已绑定到现有账户；false: 新用户或未绑定）", example = "true")
        Boolean isBound
) {
}
