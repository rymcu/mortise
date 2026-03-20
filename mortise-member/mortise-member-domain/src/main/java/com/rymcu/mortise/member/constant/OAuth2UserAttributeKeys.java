package com.rymcu.mortise.member.constant;

/**
 * OAuth2 用户属性 key 常量
 * <p>
 * 涵盖微信和通用 OAuth2 提供商返回的属性名。
 *
 * @author ronger
 */
public final class OAuth2UserAttributeKeys {

    private OAuth2UserAttributeKeys() {
    }

    // ── 微信特有属性 ──
    public static final String WECHAT_OPENID = "openid";
    public static final String WECHAT_UNIONID = "unionid";
    public static final String WECHAT_NICKNAME = "nickname";
    public static final String WECHAT_HEADIMGURL = "headimgurl";
    public static final String WECHAT_SEX = "sex";

    // ── 通用属性 ──
    public static final String NAME = "name";
    public static final String AVATAR_URL = "avatar_url";
    public static final String COUNTRY = "country";
    public static final String PROVINCE = "province";
    public static final String CITY = "city";

    // ── 默认昵称前缀 ──
    public static final String DEFAULT_WECHAT_NICKNAME_PREFIX = "WeChat_User_";
    public static final String OAUTH2_USERNAME_PREFIX = "oauth2_";
}
