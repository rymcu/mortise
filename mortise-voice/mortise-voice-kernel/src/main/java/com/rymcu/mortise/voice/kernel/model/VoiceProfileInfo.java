package com.rymcu.mortise.voice.kernel.model;

/**
 * 公开语音配置视图。
 */
public record VoiceProfileInfo(
        String code,
        String name,
        String language,
        boolean defaultProfile
) {
}