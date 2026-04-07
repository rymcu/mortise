package com.rymcu.mortise.voice.api.contract.response;

import java.time.LocalDateTime;

/**
 * 用户端语音产物响应。
 */
public record VoiceArtifactResponse(
        Long artifactId,
        String artifactType,
        String fileUrl,
        String filename,
        String originalFilename,
        String contentType,
        LocalDateTime createdTime
) {
}