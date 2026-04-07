package com.rymcu.mortise.voice.model;

/**
 * 语音任务分页查询条件。
 */
public record VoiceJobSearchCriteria(
        String query,
        String status,
        String jobType,
        Long profileId,
        Long userId
) {
}