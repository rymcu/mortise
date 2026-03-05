package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求
 *
 * @author ronger
 */
public record ResetPasswordRequest(
        @NotBlank(message = "类型不能为空")
        String type,
        @NotBlank(message = "账号不能为空")
        String account,
        @NotBlank(message = "验证码不能为空")
        String code,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "密码至少为 6 个字符")
        String newPassword
) {
}
