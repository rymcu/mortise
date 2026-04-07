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
 * 语音产物持久化对象。
 */
@Data
@Table(value = "mortise_voice_artifact", schema = "mortise")
public class VoiceArtifactPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    @Column("job_id")
    private Long jobId;

    @Column("file_id")
    private Long fileId;

    @Column("artifact_type")
    private String artifactType;

    @Column("content_type")
    private String contentType;

    private String bucket;

    @Column("object_key")
    private String objectKey;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}