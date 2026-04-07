package com.rymcu.mortise.voice.model;

/**
 * 语音模型分页查询条件。
 */
public record VoiceModelSearchCriteria(
        String query,
        Integer status,
        Long providerId,
        String capability
) {
}