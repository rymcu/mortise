package com.rymcu.mortise.voice.kernel.model;

import java.util.List;

/**
 * 一次性识别结果。
 */
public record AsrResponse(
        Long jobId,
        Long artifactId,
        String text,
        String language,
        Double durationSeconds,
        List<String> tokens,
        List<Double> timestamps
) {
}