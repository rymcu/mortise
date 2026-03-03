package com.rymcu.mortise.system.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 确认邮箱更换请求
 *
 * @param newEmail 新邮箱地址
 * @param code     验证码
 * @author ronger
 */
public record EmailUpdateConfirmInfo(
        @NotBlank(message = "新邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String newEmail,

        @NotBlank(message = "验证码不能为空")
        String code
) {
}
