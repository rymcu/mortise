package com.rymcu.mortise.voice.application.result;

/**
 * 同步语音合成结果。
 */
public record VoiceSynthesizeResult(
        Long jobId,
        Long artifactId,
        String format,
        String downloadUrl
) {
}