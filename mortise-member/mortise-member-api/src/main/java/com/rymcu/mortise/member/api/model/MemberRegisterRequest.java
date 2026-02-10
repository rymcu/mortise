package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 会员注册请求
 *
 * @author ronger
 */
public record MemberRegisterRequest(
        String username,
        String email,
        String phone,
        String nickname,
        @NotBlank(message = "密码不能为空")
        String password
) {
}
