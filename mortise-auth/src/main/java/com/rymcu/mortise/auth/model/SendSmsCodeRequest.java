package com.rymcu.mortise.auth.model;

import lombok.Data;

/**
 * 发送短信验证码请求
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
@Data
public class SendSmsCodeRequest {

    private String mobile;

    private String userType;
}
