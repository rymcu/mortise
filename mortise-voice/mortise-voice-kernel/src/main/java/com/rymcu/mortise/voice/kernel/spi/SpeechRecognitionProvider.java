package com.rymcu.mortise.voice.kernel.spi;

import com.rymcu.mortise.voice.kernel.model.AsrRequest;
import com.rymcu.mortise.voice.kernel.model.AsrResponse;
import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;

/**
 * 语音识别提供商。
 */
public interface SpeechRecognitionProvider {

    VoiceProviderType getProviderType();

    boolean isAvailable();

    AsrResponse recognizeOnce(AsrRequest request);
}