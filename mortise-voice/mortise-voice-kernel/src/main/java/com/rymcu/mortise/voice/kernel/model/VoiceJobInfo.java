package com.rymcu.mortise.voice.kernel.model;

/**
 * 语音任务视图。
 */
public record VoiceJobInfo(
        Long jobId,
        String jobType,
        String status,
        String summary
) {
}