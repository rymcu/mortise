package com.rymcu.mortise.voice.application.result;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 语音任务结果。
 */
public record VoiceJobResult(
        Long id,
        String jobType,
        String status,
        Long profileId,
        String profileName,
        String profileCode,
        Long userId,
        String sourceModule,
        Long durationMillis,
        String resultSummary,
        String errorMessage,
        List<VoiceArtifactResult> artifacts,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}