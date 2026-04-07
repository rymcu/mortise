package com.rymcu.mortise.voice.infra.persistence.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 语音模型持久化对象。
 */
@Data
@Table(value = "mortise_voice_model", schema = "mortise")
public class VoiceModelPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    @Column("provider_id")
    private Long providerId;

    private String name;

    private String code;

    private String capability;

    @Column("model_type")
    private String modelType;

    @Column("runtime_name")
    private String runtimeName;

    private String version;

    private String language;

    @Column("concurrency_limit")
    private Integer concurrencyLimit;

    @Column("is_default_model")
    private Boolean defaultModel;

    private Integer status;

    private String remark;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}