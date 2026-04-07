package com.rymcu.mortise.voice.application.query;

/**
 * 语音模型分页查询条件。
 */
public record VoiceModelSearchQuery(
        String query,
        Integer status,
        Long providerId,
        String capability
) {
}