package com.rymcu.mortise.auth.constant;

/**
 * JWT 常量定义
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/4/14
 */
public class JwtConstants {

    public static final String AUTHORIZATION = "Authorization";
    public static final String UPLOAD_TOKEN = "X-Upload-Token";
    public static final String CURRENT_USER_NAME = "CURRENT_TOKEN_USER_NAME";
    public static final String CURRENT_TOKEN_CLAIMS = "CURRENT_TOKEN_CLAIMS";

    public static final long TOKEN_EXPIRES_HOUR = 2;
    public static final long LAST_ONLINE_EXPIRES_MINUTE = 10;
    public static final long TOKEN_EXPIRES_MINUTE = 15;
    public static final long REFRESH_TOKEN_EXPIRES_HOUR = 2;

    /**
     * 私有构造函数，防止实例化
     */
    private JwtConstants() {
        throw new AssertionError("常量类不应该被实例化");
    }
}
