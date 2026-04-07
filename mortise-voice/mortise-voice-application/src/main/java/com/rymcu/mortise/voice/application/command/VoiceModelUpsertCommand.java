package com.rymcu.mortise.voice.application.command;

/**
 * 语音模型新增/更新命令。
 */
public record VoiceModelUpsertCommand(
        Long providerId,
        String name,
        String code,
        String capability,
        String modelType,
        String runtimeName,
        String version,
        String language,
        Integer concurrencyLimit,
        Boolean defaultModel,
        Integer status,
        String remark
) {
}