package com.rymcu.mortise.voice.application.result;

import java.time.LocalDateTime;

/**
 * 配置目录结果。
 */
public record VoiceProfileResult(
        Long id,
        String name,
        String code,
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
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}