package com.rymcu.mortise.voice.api.contract.response;

import java.time.LocalDateTime;

/**
 * 用户端语音任务列表响应。
 */
public record VoiceJobSummaryResponse(
        Long jobId,
        String jobType,
        String status,
        String profileCode,
        String profileName,
        Long durationMillis,
        String resultSummary,
        String errorMessage,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}