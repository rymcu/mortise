package com.rymcu.mortise.auth.model;

import lombok.Data;

/**
 * 短信验证码登录请求
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
@Data
public class SmsLoginRequest {

    private String mobile;

    private String smsCode;

    private String userType;
}
