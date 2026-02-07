package com.rymcu.mortise.auth.constant;

/**
 * 认证授权模块缓存常量
 *
 * <p>包含 JWT、OAuth2、会话管理等认证相关的缓存配置</p>
 *
 * @author ronger
 * @since 1.0.0
 */
public class AuthCacheConstant {

    // ==================== JWT Token 缓存 ====================

    /**
     * JWT Token 缓存
     */
    public static final String JWT_TOKEN_CACHE = "jwt:token";
    public static final long JWT_TOKEN_EXPIRE_MINUTES = 30;

    /**
     * JWT Token 黑名单缓存
     * <p>
     * 用于存储已注销的 Token，防止被注销的 Token 继续使用
     * 缓存 key 格式: jwt:blacklist:{jti}
     * </p>
     */
    public static final String JWT_TOKEN_BLACKLIST_CACHE = "jwt:blacklist";

    // ==================== OAuth2 缓存 ====================

    /**
     * OAuth2 授权请求缓存
     */
    public static final String OAUTH2_AUTHORIZATION_REQUEST_CACHE = "oauth2:auth-request";
    public static final long OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES = 10;

    /**
     * OAuth2 授权请求缓存
     */
    public static final String OAUTH2_PARAMETER_MAP_CACHE = "oauth2:auth-parameter";
    public static final long OAUTH2_PARAMETER_MAP_EXPIRE_MINUTES = 15;
    /**
     * OAuth2 二维码状态缓存
     */
    public static final String OAUTH2_QRCODE_STATE_CACHE = "oauth2:auth-qrcode";
    public static final long OAUTH2_QRCODE_STATE_EXPIRE_MINUTES = 10;

    /**
     * OAuth2 登录响应缓存（用于 state 兑换 token）
     */
    public static final String OAUTH2_LOGIN_RESPONSE_CACHE = "oauth2:login-response";
    public static final long OAUTH2_LOGIN_RESPONSE_EXPIRE_MINUTES = 5;

    /**
     * 会员刷新令牌缓存
     */
    public static final String MEMBER_REFRESH_TOKEN_CACHE = "member:refresh-token";
    public static final long MEMBER_REFRESH_TOKEN_EXPIRE_HOURS = 24;

    // ==================== 验证码缓存 ====================

    /**
     * 通用验证码缓存
     */
    public static final String VERIFICATION_CODE_CACHE = "verification:code";
    public static final long VERIFICATION_CODE_EXPIRE_MINUTES = 5;

    /**
     * 短信验证码缓存（支持多用户类型）
     * <p>
     * 缓存key格式: sms:code:{userType}:{mobile}
     * </p>
     */
    public static final String SMS_CODE_CACHE = "sms:code";
    public static final long SMS_CODE_EXPIRE_MINUTES = 5;

    /**
     * 短信验证码发送限制缓存
     * <p>
     * 防止验证码发送过于频繁，例如60秒内只能发送一次
     * 缓存key格式: sms:limit:{userType}:{mobile}
     * </p>
     */
    public static final String SMS_CODE_SEND_LIMIT_CACHE = "sms:limit";
    public static final long SMS_CODE_SEND_LIMIT_SECONDS = 60;

    private AuthCacheConstant() {
        // 工具类禁止实例化
    }
}
