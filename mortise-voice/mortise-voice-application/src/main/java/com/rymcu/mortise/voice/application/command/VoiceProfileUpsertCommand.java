package com.rymcu.mortise.voice.application.command;

/**
 * 语音配置新增/更新命令。
 */
public record VoiceProfileUpsertCommand(
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
        String remark
) {
}