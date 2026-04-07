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
 * 语音提供商持久化对象。
 */
@Data
@Table(value = "mortise_voice_provider", schema = "mortise")
public class VoiceProviderPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    private String name;

    private String code;

    @Column("provider_type")
    private String providerType;

    private Integer status;

    @Column("sort_no")
    private Integer sortNo;

    @Column("default_config")
    private String defaultConfig;

    private String remark;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}