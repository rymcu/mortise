package com.rymcu.mortise.voice.api.contract.response;

/**
 * 用户端语音配置选项。
 */
public record VoiceProfileOption(
        String code,
        String name,
        String language,
        boolean defaultProfile
) {
}