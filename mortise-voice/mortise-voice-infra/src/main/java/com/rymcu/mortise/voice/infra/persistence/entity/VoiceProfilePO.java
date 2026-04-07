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
 * 语音配置持久化对象。
 */
@Data
@Table(value = "mortise_voice_profile", schema = "mortise")
public class VoiceProfilePO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    private String name;

    private String code;

    private String language;

    @Column("asr_provider_id")
    private Long asrProviderId;

    @Column("asr_model_id")
    private Long asrModelId;

    @Column("vad_provider_id")
    private Long vadProviderId;

    @Column("vad_model_id")
    private Long vadModelId;

    @Column("tts_provider_id")
    private Long ttsProviderId;

    @Column("tts_model_id")
    private Long ttsModelId;

    @Column("default_params")
    private String defaultParams;

    private Integer status;

    @Column("sort_no")
    private Integer sortNo;

    private String remark;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}