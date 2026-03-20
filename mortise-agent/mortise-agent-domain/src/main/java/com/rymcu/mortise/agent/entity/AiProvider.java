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
 * AI 提供商实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_ai_provider", schema = "mortise")
public class AiProvider implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /** 提供商名称（如 "OpenAI"） */
    private String name;

    /** 提供商编码（对应 ModelType.code，如 "openai"），唯一 */
    private String code;

    /** API 密钥（建议加密存储） */
    private String apiKey;

    /** 自定义 API 地址 */
    private String baseUrl;

    /** 默认模型名称 */
    private String defaultModelName;

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
