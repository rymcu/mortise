package com.rymcu.mortise.voice.kernel.constant;

/**
 * 语音模块常量。
 */
public final class VoiceConstants {

    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 1500;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30_000;
    public static final long DEFAULT_MAX_ASR_FILE_SIZE = 20L * 1024 * 1024;
    public static final int DEFAULT_MAX_ASR_DURATION_SECONDS = 120;
    public static final int DEFAULT_MAX_TTS_TEXT_LENGTH = 1000;

    private VoiceConstants() {
    }
}