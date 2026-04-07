package com.rymcu.mortise.voice.application.query;

/**
 * 语音任务分页查询条件。
 */
public record VoiceJobSearchQuery(
        String query,
        String status,
        String jobType,
        Long profileId,
        Long userId
) {
}