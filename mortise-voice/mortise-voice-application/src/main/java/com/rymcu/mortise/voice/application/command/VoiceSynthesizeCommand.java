package com.rymcu.mortise.voice.application.command;

/**
 * 同步语音合成命令。
 */
public record VoiceSynthesizeCommand(
        Long userId,
        String profileCode,
        String text,
        String voiceName,
        String sourceModule
) {
}