package com.rymcu.mortise.voice.application.result;

import java.time.LocalDateTime;

/**
 * 语音产物结果。
 */
public record VoiceArtifactResult(
        Long id,
        Long fileId,
        String artifactType,
        String contentType,
        String bucket,
        String objectKey,
        String fileUrl,
        String filename,
        String originalFilename,
        LocalDateTime createdTime
) {
}