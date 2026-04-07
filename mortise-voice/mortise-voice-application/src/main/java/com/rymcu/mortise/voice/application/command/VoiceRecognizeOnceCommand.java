package com.rymcu.mortise.voice.application.command;

/**
 * 短音频同步识别命令。
 */
public record VoiceRecognizeOnceCommand(
        Long userId,
        String profileCode,
        String fileName,
        String contentType,
        long size,
        byte[] content,
        String sourceModule
) {
}