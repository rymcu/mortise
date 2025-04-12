package com.rymcu.mortise.model;

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
    private String account;

    /**
     * 密码
     */
    private String password;
}
