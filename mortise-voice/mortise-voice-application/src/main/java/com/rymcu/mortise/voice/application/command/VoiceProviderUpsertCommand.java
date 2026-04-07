package com.rymcu.mortise.voice.application.command;

/**
 * 语音提供商新增/更新命令。
 */
public record VoiceProviderUpsertCommand(
        String name,
        String code,
        String providerType,
        Integer status,
        Integer sortNo,
        String defaultConfig,
        String remark
) {
}