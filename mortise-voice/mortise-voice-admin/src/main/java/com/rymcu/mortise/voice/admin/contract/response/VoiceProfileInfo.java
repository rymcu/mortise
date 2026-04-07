package com.rymcu.mortise.voice.admin.contract.response;

import java.time.LocalDateTime;

/**
 * 管理端配置组合信息。
 */
public record VoiceProfileInfo(
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