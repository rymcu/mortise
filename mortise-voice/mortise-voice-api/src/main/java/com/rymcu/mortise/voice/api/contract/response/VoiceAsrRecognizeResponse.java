package com.rymcu.mortise.voice.api.contract.response;

import java.util.List;

/**
 * 短音频同步识别响应。
 */
public record VoiceAsrRecognizeResponse(
        Long jobId,
        String text,
        String language,
        Double durationSeconds,
        List<String> tokens,
        List<Double> timestamps
) {
}