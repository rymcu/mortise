package com.rymcu.mortise.member.constant;

/**
 * 会员 JWT 令牌相关常量
 *
 * @author ronger
 */
public final class MemberJwtConstants {

    private MemberJwtConstants() {
    }

    // ── JWT claim keys ──
    public static final String CLAIM_MEMBER_ID = "memberId";
    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_LOGIN_TYPE = "loginType";
    public static final String CLAIM_OPENID = "openid";
    public static final String CLAIM_UNIONID = "unionid";
    public static final String CLAIM_SESSION_ID = "sid";
    public static final String CLAIM_CLIENT_ID = "clientId";
    public static final String CLAIM_LOGIN_CHANNEL = "loginChannel";
    public static final String CLAIM_SCOPE = "scope";

    // ── claim values ──
    public static final String TYPE_MEMBER = "member";
    public static final String LOGIN_TYPE_PHONE = "phone";
    public static final String LOGIN_TYPE_OAUTH2 = "oauth2";
    public static final String LOGIN_TYPE_DESKTOP = "desktop";
    public static final String LOGIN_CHANNEL_DESKTOP_BROWSER = "desktop_browser";

    // ── token 过期时间（毫秒） ──
    /** 访问令牌过期时间：30 分钟 */
    public static final long ACCESS_TOKEN_EXPIRY_MS = 1_800_000L;
}
