package com.rymcu.mortise.wechat.entity;

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
 * 微信账号实体（支持多账号）
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mortise_wechat_account")
public class WeChatAccount implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 账号类型：mp-公众号, open-开放平台, miniapp-小程序
     */
    @Column("account_type")
    private String accountType;

    /**
     * 账号名称（便于识别）
     */
    @Column("account_name")
    private String accountName;

    /**
     * 微信 AppID
     */
    @Column("app_id")
    private String appId;

    /**
     * AppSecret（加密存储）
     */
    @Column("app_secret")
    private String appSecret;

    /**
     * 是否为默认账号：0-否, 1-是
     */
    @Column("is_default")
    private Integer isDefault;

    /**
     * 是否启用：0-禁用, 1-启用
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
     * 备注说明
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
