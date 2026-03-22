package com.rymcu.mortise.member.constant;

/**
 * 会员 OAuth2 相关常量。
 */
public final class MemberOAuth2Constants {

    private MemberOAuth2Constants() {
    }

    public static final String DEFAULT_WECHAT_REGISTRATION_ID = "wechat-app";
    public static final String GITHUB_APP_REGISTRATION_ID = "github-app";
    public static final String GOOGLE_APP_REGISTRATION_ID = "google-app";
    public static final String LOGTO_APP_REGISTRATION_ID = "logto-app";
    public static final String DEFAULT_WECHAT_SCOPE = "snsapi_userinfo";
    public static final String DEFAULT_APP_ID = "";
    public static final String DEFAULT_QR_CODE_EXPIRE_SECONDS_VALUE = "300";
    public static final String DEFAULT_MEMBER_ROLE = "ROLE_USER";
    public static final String AUTH_TYPE_WECHAT_REDIRECT = "wechat_redirect";
    public static final String AUTH_TYPE_STANDARD = "standard";
    public static final int QR_CODE_EXPIRE_SECONDS_MIN = 60;
    public static final int QR_CODE_EXPIRE_SECONDS_DEFAULT = 300;
    public static final int QR_CODE_EXPIRE_SECONDS_MAX = 300;
    public static final String QR_CODE_SCENE_PREFIX = "LOGIN_";
    public static final String UUID_HYPHEN = "-";
    public static final String WECHAT_SHOW_QRCODE_URL_PREFIX = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=";
}