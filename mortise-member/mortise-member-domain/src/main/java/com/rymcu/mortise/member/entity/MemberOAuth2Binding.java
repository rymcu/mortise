package com.rymcu.mortise.member.entity;

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
 * 会员 OAuth2 绑定实体
 * <p>
 * 用于记录会员与 OAuth2 提供商的绑定关系
 * 一个会员可以绑定多个 OAuth2 账号（微信公众号、微信小程序、QQ、微博等）
 * <p>
 * 参考 mortise-system 模块的 {@code UserOAuth2Binding} 实体设计
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Table(value = "mortise_member_oauth2_binding", schema = "mortise")
public class MemberOAuth2Binding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * OAuth2 提供商
     * wechat - 微信开放平台
     * wechat_mp - 微信公众号
     * wechat_miniapp - 微信小程序
     * qq - QQ
     * weibo - 微博
     */
    private String provider;

    /**
     * OAuth2 提供商的用户唯一标识
     * - 微信: openid
     * - QQ: openid
     * - 微博: uid
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
     * OAuth2 提供商返回的手机号
     */
    private String phone;

    /**
     * 性别: male-男, female-女, other-其他
     */
    private String gender;

    /**
     * 国家
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
     * 状态: 0-正常, 1-禁用
     */
    private Integer status;

    /**
     * 删除标记: 0-未删除, 1-已删除
     */
    private Integer delFlag;

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
