package com.rymcu.mortise.voice.api.contract.response;

/**
 * 用户端同步语音合成响应。
 */
public record VoiceTtsSynthesizeResponse(
        Long jobId,
        Long artifactId,
        String format,
        String downloadUrl
) {
}