package com.rymcu.mortise.system.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱更换验证码请求
 *
 * @param newEmail 新邮箱地址
 * @author ronger
 */
public record EmailUpdateCodeRequest(
        @NotBlank(message = "新邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String newEmail
) {
}
