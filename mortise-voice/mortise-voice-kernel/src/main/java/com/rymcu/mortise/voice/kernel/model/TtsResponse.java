package com.rymcu.mortise.voice.kernel.model;

/**
 * 语音合成结果。
 */
public record TtsResponse(
        Long jobId,
        Long artifactId,
        String format,
        String downloadUrl,
        String contentType,
        byte[] content
) {
}