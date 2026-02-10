package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 发送验证码请求
 *
 * @author ronger
 */
public record SendCodeRequest(
        @NotBlank(message = "类型不能为空")
        String type,
        String phone,
        String email
) {
}
