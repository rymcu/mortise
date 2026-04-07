package com.rymcu.mortise.voice.kernel.model;

/**
 * 语音合成请求。
 */
public record TtsRequest(
        String profileCode,
        String text,
        String voiceName
) {
}