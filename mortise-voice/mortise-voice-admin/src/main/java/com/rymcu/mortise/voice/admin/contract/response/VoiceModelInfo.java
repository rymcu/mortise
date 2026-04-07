package com.rymcu.mortise.voice.admin.contract.response;

import java.time.LocalDateTime;

/**
 * 管理端模型信息。
 */
public record VoiceModelInfo(
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