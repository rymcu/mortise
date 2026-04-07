package com.rymcu.mortise.voice.model;

/**
 * 语音提供商分页查询条件。
 */
public record VoiceProviderSearchCriteria(
        String query,
        Integer status
) {
}