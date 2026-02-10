package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 会员登录请求
 *
 * @author ronger
 */
public record MemberLoginRequest(
        @NotBlank(message = "账号不能为空")
        String account,
        @NotBlank(message = "密码不能为空")
        String password
) {
}
