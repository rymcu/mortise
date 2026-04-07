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
 * 语音任务持久化对象。
 */
@Data
@Table(value = "mortise_voice_job", schema = "mortise")
public class VoiceJobPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    @Column("job_type")
    private String jobType;

    private String status;

    @Column("profile_id")
    private Long profileId;

    @Column("user_id")
    private Long userId;

    @Column("source_module")
    private String sourceModule;

    @Column("duration_millis")
    private Long durationMillis;

    @Column("result_summary")
    private String resultSummary;

    @Column("error_message")
    private String errorMessage;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}