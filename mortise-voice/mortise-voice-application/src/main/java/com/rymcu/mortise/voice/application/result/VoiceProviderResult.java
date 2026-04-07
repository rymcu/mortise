package com.rymcu.mortise.voice.application.result;

import java.time.LocalDateTime;

/**
 * 提供商目录结果。
 */
public record VoiceProviderResult(
        Long id,
        String name,
        String code,
        String providerType,
        Integer status,
        Integer sortNo,
        String defaultConfig,
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}