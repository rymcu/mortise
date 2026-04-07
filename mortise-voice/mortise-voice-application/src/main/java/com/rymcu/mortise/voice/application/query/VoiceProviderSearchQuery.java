package com.rymcu.mortise.voice.application.query;

/**
 * 语音提供商分页查询条件。
 */
public record VoiceProviderSearchQuery(
        String query,
        Integer status
) {
}