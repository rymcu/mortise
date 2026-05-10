package com.rymcu.mortise.member.constant;

/**
 * 桌面端授权桥常量。
 *
 * @author ronger
 */
public final class DesktopOAuthConstants {

    private DesktopOAuthConstants() {
    }

    public static final String RODAK_CLIENT_ID = "rodak-desktop";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";
    public static final String DEFAULT_SCOPE = "profile";
    public static final String SESSION_STATUS_ACTIVE = "active";
    public static final String SESSION_STATUS_REVOKED = "revoked";
    public static final String AUTHORIZATION_CODE_CACHE = "desktop:oauth:code";
    public static final String REFRESH_SESSION_CACHE = "desktop:oauth:refresh-session";
    public static final String SESSION_REFRESH_INDEX_CACHE = "desktop:oauth:session-refresh";
    public static final long AUTHORIZATION_CODE_EXPIRE_MINUTES = 5;
}
