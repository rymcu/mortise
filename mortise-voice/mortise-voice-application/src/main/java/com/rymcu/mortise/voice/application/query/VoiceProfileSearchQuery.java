package com.rymcu.mortise.voice.application.query;

/**
 * 语音配置分页查询条件。
 */
public record VoiceProfileSearchQuery(
        String query,
        Integer status
) {
}