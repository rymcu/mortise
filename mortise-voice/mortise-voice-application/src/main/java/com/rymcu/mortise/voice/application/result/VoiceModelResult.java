package com.rymcu.mortise.voice.application.result;

import java.time.LocalDateTime;

/**
 * 模型目录结果。
 */
public record VoiceModelResult(
        Long id,
        Long providerId,
        String name,
        String code,
        String capability,
        String modelType,
        String runtimeName,
        String version,
        String language,
        Integer status,
        Integer concurrencyLimit,
        Boolean defaultModel,
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}