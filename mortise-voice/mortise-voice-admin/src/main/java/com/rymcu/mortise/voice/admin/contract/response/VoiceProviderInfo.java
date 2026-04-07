package com.rymcu.mortise.voice.admin.contract.response;

import java.time.LocalDateTime;

/**
 * 管理端提供商信息。
 */
public record VoiceProviderInfo(
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