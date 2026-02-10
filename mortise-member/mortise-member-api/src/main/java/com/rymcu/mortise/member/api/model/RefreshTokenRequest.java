package com.rymcu.mortise.member.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 刷新 Token 请求
 *
 * @author ronger
 */
@Schema(description = "刷新 Token 请求")
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken 不能为空")
        @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {
}
