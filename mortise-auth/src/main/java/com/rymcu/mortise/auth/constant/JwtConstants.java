package com.rymcu.mortise.auth.constant;

/**
 * JWT 常量定义
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/4/14
 */
public class JwtConstants {

    /**
     * HTTP Authorization 请求头名称
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * JWT Token 默认过期时间（分钟）
     */
    public static final long TOKEN_EXPIRES_MINUTE = 15;

    /**
     * 私有构造函数，防止实例化
     */
    private JwtConstants() {
        throw new AssertionError("常量类不应该被实例化");
    }
}
