package com.rymcu.mortise.voice.admin.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 语音模型新增/更新请求。
 */
public record VoiceModelUpsertRequest(
        @NotNull Long providerId,
        @NotBlank String name,
        @NotBlank String code,
        @NotBlank String capability,
        @NotBlank String modelType,
        String runtimeName,
        String version,
        String language,
        Integer concurrencyLimit,
        Boolean defaultModel,
        Integer status,
        String remark
) {
}