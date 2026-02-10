package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 验证验证码请求
 *
 * @author ronger
 */
public record VerifyCodeRequest(
        @NotBlank(message = "类型不能为空")
        String type,
        @NotBlank(message = "验证码不能为空")
        String code,
        String phone,
        String email
) {
}
