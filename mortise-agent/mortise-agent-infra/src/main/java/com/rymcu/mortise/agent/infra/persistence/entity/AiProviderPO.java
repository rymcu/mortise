package com.rymcu.mortise.agent.infra.persistence.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 提供商持久化对象。
 */
@Data
@Table(value = "mortise_ai_provider", schema = "mortise")
public class AiProviderPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    private String name;

    private String code;

    private String apiKey;

    private String baseUrl;

    private String defaultModelName;

    private Integer status;

    private Integer sortNo;

    private String remark;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}
