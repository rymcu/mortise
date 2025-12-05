package com.rymcu.mortise.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码登录配置属性
 * <p>
 * 支持通过配置文件自定义验证码登录的各项参数
 * </p>
 *
 * <p>配置示例：</p>
 * <pre>
 * mortise:
 *   auth:
 *     sms:
 *       login-url: /api/v1/auth/login/sms
 *       mobile-parameter: mobile
 *       code-parameter: smsCode
 *       user-type-parameter: userType
 *       post-only: true
 * </pre>
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "mortise.auth.sms")
public class SmsAuthProperties {

    /**
     * 短信验证码登录 URL
     */
    private String loginUrl = "/api/v1/auth/login/sms";

    /**
     * 手机号参数名
     */
    private String mobileParameter = "mobile";

    /**
     * 验证码参数名
     */
    private String codeParameter = "smsCode";

    /**
     * 用户类型参数名
     */
    private String userTypeParameter = "userType";

    /**
     * 是否仅支持 POST 请求
     */
    private boolean postOnly = true;
}
