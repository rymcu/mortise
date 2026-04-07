package com.rymcu.mortise.voice.admin.contract.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 语音提供商新增/更新请求。
 */
public record VoiceProviderUpsertRequest(
        @NotBlank String name,
        @NotBlank String code,
        @NotBlank String providerType,
        Integer status,
        Integer sortNo,
        String defaultConfig,
        String remark
) {
}