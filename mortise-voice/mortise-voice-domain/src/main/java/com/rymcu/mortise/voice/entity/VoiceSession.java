package com.rymcu.mortise.voice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实时语音会话领域对象。
 */
@Data
public class VoiceSession {

    private Long id;

    private String sessionCode;

    private String status;

    private Long userId;

    private Long profileId;

    private Integer inputFrameCount;

    private Integer finalSegmentCount;

    private LocalDateTime startedTime;

    private LocalDateTime endedTime;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}