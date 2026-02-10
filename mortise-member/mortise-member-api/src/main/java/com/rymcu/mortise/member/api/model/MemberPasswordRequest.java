package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 会员密码修改请求
 *
 * @author ronger
 */
public record MemberPasswordRequest(
        String oldPassword,
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {
}
