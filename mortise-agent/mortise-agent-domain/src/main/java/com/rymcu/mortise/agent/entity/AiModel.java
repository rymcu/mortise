package com.rymcu.mortise.agent.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 模型实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_ai_model", schema = "mortise")
public class AiModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /** 所属提供商 ID */
    private Long providerId;

    /** 模型名称（如 "gpt-4.1"） */
    private String modelName;

    /** 显示名称（如 "GPT-4.1"） */
    private String displayName;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;

    /** 排序号 */
    private Integer sortNo;

    /** 备注 */
    private String remark;

    /** 逻辑删除 */
    @Column(isLogicDelete = true)
    private Integer delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}
