package com.rymcu.mortise.voice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语音任务领域对象。
 */
@Data
public class VoiceJob {

    private Long id;

    private String jobType;

    private String status;

    private Long profileId;

    private Long userId;

    private String sourceModule;

    private Long durationMillis;

    private String resultSummary;

    private String errorMessage;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}