package com.rymcu.mortise.voice.api.contract.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端语音任务详情响应。
 */
public record VoiceJobDetailResponse(
        Long jobId,
        String jobType,
        String status,
        String profileCode,
        String profileName,
        Long durationMillis,
        String resultSummary,
        String errorMessage,
        List<VoiceArtifactResponse> artifacts,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}