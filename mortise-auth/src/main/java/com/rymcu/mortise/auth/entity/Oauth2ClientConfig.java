package com.rymcu.mortise.auth.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OAuth2 客户端配置实体
 * <p>
 * 用于存储动态的 OAuth2 客户端配置信息，支持运行时动态添加/修改/删除客户端
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mortise_oauth2_client_config")
public class Oauth2ClientConfig implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 客户端注册ID（唯一标识）
     * 例如：wechat_company_a, github, google 等
     */
    @Column("registration_id")
    private String registrationId;

    /**
     * OAuth2 客户端 ID
     */
    @Column("client_id")
    private String clientId;

    /**
     * OAuth2 客户端密钥
     */
    @Column("client_secret")
    private String clientSecret;

    /**
     * 客户端名称（显示给用户的名称）
     */
    @Column("client_name")
    private String clientName;

    /**
     * 授权范围（逗号分隔）
     * 例如：openid,profile,email
     */
    @Column("scopes")
    private String scopes;

    /**
     * 重定向 URI 模板
     * 例如：{baseUrl}/login/oauth2/code/{registrationId}
     */
    @Column("redirect_uri_template")
    private String redirectUriTemplate;

    /**
     * 客户端认证方法
     * 例如：client_secret_basic, client_secret_post
     */
    @Column("client_authentication_method")
    private String clientAuthenticationMethod;

    /**
     * 授权类型
     * 例如：authorization_code, implicit, password, client_credentials
     */
    @Column("authorization_grant_type")
    private String authorizationGrantType;

    /**
     * 授权端点 URI
     */
    @Column("authorization_uri")
    private String authorizationUri;

    /**
     * Token 端点 URI
     */
    @Column("token_uri")
    private String tokenUri;

    /**
     * 用户信息端点 URI
     */
    @Column("user_info_uri")
    private String userInfoUri;

    /**
     * 用户名属性名称
     * 例如：sub, name, login 等
     */
    @Column("user_name_attribute")
    private String userNameAttribute;

    /**
     * JWK Set URI（用于验证 JWT）
     */
    @Column("jwk_set_uri")
    private String jwkSetUri;

    /**
     * 是否启用
     */
    @Column("is_enabled")
    private Integer isEnabled;

    /**
     * 状态：0-正常, 1-禁用
     */
    @Column("status")
    private Integer status;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column("del_flag")
    private Integer delFlag;

    /**
     * 备注
     */
    @Column("remark")
    private String remark;

    /**
     * 创建人ID
     */
    @Column("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新人ID
     */
    @Column("updated_by")
    private Long updatedBy;

    /**
     * 更新时间
     */
    @Column("updated_time")
    private LocalDateTime updatedTime;
}
