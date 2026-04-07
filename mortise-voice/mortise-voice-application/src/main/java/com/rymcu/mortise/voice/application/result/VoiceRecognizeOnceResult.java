package com.rymcu.mortise.voice.application.result;

import java.util.List;

/**
 * 短音频同步识别结果。
 */
public record VoiceRecognizeOnceResult(
        Long jobId,
        String text,
        String language,
        Double durationSeconds,
        List<String> tokens,
        List<Double> timestamps
) {
}