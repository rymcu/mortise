package com.rymcu.mortise.voice.admin.contract.response;

import java.time.LocalDateTime;

/**
 * 管理端语音产物信息。
 */
public record VoiceArtifactInfo(
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