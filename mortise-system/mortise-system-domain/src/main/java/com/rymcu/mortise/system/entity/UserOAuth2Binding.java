package com.rymcu.mortise.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 OAuth2 绑定实体
 * <p>
 * 用于记录用户与 OAuth2 提供商的绑定关系
 * 一个用户可以绑定多个 OAuth2 账号（GitHub、Google、微信、Logto等）
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Table(value = "mortise_user_oauth2_binding", schema = "mortise")
public class UserOAuth2Binding implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * OAuth2 提供商
     * github, google, wechat, logto
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
     * OAuth2 提供商返回的昵称
     */
    private String nickname;

    /**
     * OAuth2 提供商返回的头像
     */
    private String avatar;

    /**
     * OAuth2 提供商返回的邮箱
     */
    private String email;

    /**
     * 访问令牌（可选，如需调用第三方 API）
     */
    @Column(isLarge = true)
    private String accessToken;

    /**
     * 刷新令牌（可选）
     */
    @Column(isLarge = true)
    private String refreshToken;

    /**
     * 令牌过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * 原始用户数据 (JSON)
     */
    @Column(isLarge = true)
    private String rawData;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
