package com.rymcu.mortise.voice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语音产物领域对象。
 */
@Data
public class VoiceArtifact {

    private Long id;

    private Long jobId;

    private Long fileId;

    private String artifactType;

    private String contentType;

    private String bucket;

    private String objectKey;

    private Integer delFlag;

    private LocalDateTime createdTime;
}