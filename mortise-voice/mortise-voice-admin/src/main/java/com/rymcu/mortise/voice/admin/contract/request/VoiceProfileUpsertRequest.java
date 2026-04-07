package com.rymcu.mortise.voice.admin.contract.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 语音配置新增/更新请求。
 */
public record VoiceProfileUpsertRequest(
        @NotBlank String name,
        @NotBlank String code,
        String language,
        Long asrProviderId,
        Long asrModelId,
        Long vadProviderId,
        Long vadModelId,
        Long ttsProviderId,
        Long ttsModelId,
        String defaultParams,
        Integer status,
        Integer sortNo,
        String remark
) {
}