package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 修改用户名请求
 *
 * @author ronger
 */
public record MemberUsernameRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须是4-20位字母、数字或下划线")
        String newUsername
) {
}
