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
 * 微信账号配置实体
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mortise_wechat_config")
public class WeChatConfig implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 关联的账号ID
     */
    @Column("account_id")
    private Long accountId;

    /**
     * 配置项键名
     */
    @Column("config_key")
    private String configKey;

    /**
     * 配置项值
     */
    @Column("config_value")
    private String configValue;

    /**
     * 配置项描述
     */
    @Column("config_label")
    private String configLabel;

    /**
     * 是否加密：0-否, 1-是
     */
    @Column("is_encrypted")
    private Integer isEncrypted;

    /**
     * 排序号
     */
    @Column("sort_no")
    private Integer sortNo;

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
