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
     * 认证令牌缓存（通用）
     */
    public static final String AUTH_TOKEN_CACHE = "auth:token";

    /**
     * 刷新令牌缓存
     */
    public static final String AUTH_REFRESH_TOKEN_CACHE = "auth:refresh-token";
    public static final long REFRESH_TOKEN_EXPIRE_HOURS = 24;

    // ==================== OAuth2 缓存 ====================

    /**
     * OAuth2 授权请求缓存
     */
    public static final String OAUTH2_AUTHORIZATION_REQUEST_CACHE = "oauth2:auth-request";
    public static final long OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES = 10;

    /**
     * OAuth2 授权码缓存
     */
    public static final String OAUTH2_AUTHORIZATION_CODE_CACHE = "oauth2:auth-code";
    public static final long OAUTH2_AUTHORIZATION_CODE_EXPIRE_MINUTES = 5;

    /**
     * OAuth2 访问令牌缓存
     */
    public static final String OAUTH2_ACCESS_TOKEN_CACHE = "oauth2:access-token";
    public static final long OAUTH2_ACCESS_TOKEN_EXPIRE_HOURS = 2;

    // ==================== 用户会话缓存 ====================

    /**
     * 用户会话缓存
     */
    public static final String USER_SESSION_CACHE = "user:session";
    public static final long USER_SESSION_EXPIRE_HOURS = 2;

    /**
     * 用户在线状态缓存
     */
    public static final String USER_ONLINE_STATUS_CACHE = "user:online";
    public static final long USER_ONLINE_STATUS_EXPIRE_MINUTES = 30;

    /**
     * 账号序列缓存（用于防止并发登录）
     */
    public static final String ACCOUNT_SEQUENCE_CACHE = "account:sequence";
    public static final long ACCOUNT_SEQUENCE_EXPIRE_HOURS = 8760; // 1年

    // ==================== 验证码缓存 ====================

    /**
     * 登录验证码缓存
     */
    public static final String LOGIN_VERIFICATION_CODE_CACHE = "verification:login-code";
    public static final long LOGIN_VERIFICATION_CODE_EXPIRE_MINUTES = 5;

    /**
     * 注册验证码缓存
     */
    public static final String REGISTER_VERIFICATION_CODE_CACHE = "verification:register-code";
    public static final long REGISTER_VERIFICATION_CODE_EXPIRE_MINUTES = 10;

    /**
     * 通用验证码缓存
     */
    public static final String VERIFICATION_CODE_CACHE = "verification:code";
    public static final long VERIFICATION_CODE_EXPIRE_MINUTES = 5;

    /**
     * 密码重置缓存
     */
    public static final String PASSWORD_RESET_CACHE = "password:reset";
    public static final long PASSWORD_RESET_EXPIRE_MINUTES = 30;

    /**
     * 密码重置令牌缓存
     */
    public static final String PASSWORD_RESET_TOKEN_CACHE = "password:reset-token";
    public static final long PASSWORD_RESET_TOKEN_EXPIRE_HOURS = 1;

    // ==================== 登录限制缓存 ====================

    /**
     * 登录失败次数缓存（用于限制登录尝试）
     */
    public static final String LOGIN_FAIL_COUNT_CACHE = "login:fail-count";
    public static final long LOGIN_FAIL_COUNT_EXPIRE_MINUTES = 30;

    /**
     * 账号锁定缓存
     */
    public static final String ACCOUNT_LOCK_CACHE = "account:lock";
    public static final long ACCOUNT_LOCK_EXPIRE_HOURS = 1;

    private AuthCacheConstant() {
        // 工具类禁止实例化
    }
}
