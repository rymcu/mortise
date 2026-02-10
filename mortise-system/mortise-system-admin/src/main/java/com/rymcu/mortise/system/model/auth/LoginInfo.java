package com.rymcu.mortise.system.model.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created on 2025/4/12 19:02.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class LoginInfo {
    /**
     * 登录账号
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
