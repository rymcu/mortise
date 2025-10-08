package com.rymcu.mortise.auth.spi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 标准化的 OAuth2 用户信息
 * <p>
 * 将不同 OAuth2 提供商的用户数据标准化为统一格式
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardOAuth2UserInfo {

    /**
     * OAuth2 提供商标识
     * 如: "github", "google", "wechat", "logto"
     */
    private String provider;

    /**
     * OAuth2 提供商的用户唯一标识
     * - GitHub: id
     * - Google: sub
     * - 微信: openid
     * - Logto: sub
     */
    private String openId;

    /**
     * 微信 UnionID（仅微信开放平台）
     * 用于同一主体下的多个应用关联用户
     */
    private String unionId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像 URL
     */
    private String avatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号（部分提供商支持）
     */
    private String phone;

    /**
     * 真实姓名（部分提供商支持）
     */
    private String realName;

    /**
     * 性别（部分提供商支持）
     * 0: 未知, 1: 男, 2: 女
     */
    private Integer gender;

    /**
     * 国家/地区
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 语言
     */
    private String language;

    /**
     * 访问令牌（可选，如需调用第三方 API）
     */
    private String accessToken;

    /**
     * 刷新令牌（可选）
     */
    private String refreshToken;

    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 原始用户数据
     * 保留所有提供商返回的原始字段，便于后续扩展
     */
    private Map<String, Object> rawAttributes;

    /**
     * 是否已验证邮箱
     */
    private Boolean emailVerified;

    /**
     * 是否已验证手机号
     */
    private Boolean phoneVerified;

    /**
     * 重定向地址
     */
    private String redirectUri;
}
