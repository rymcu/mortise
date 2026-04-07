package com.rymcu.mortise.voice.admin.contract.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端语音任务信息。
 */
public record VoiceJobInfo(
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
        List<VoiceArtifactInfo> artifacts,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}