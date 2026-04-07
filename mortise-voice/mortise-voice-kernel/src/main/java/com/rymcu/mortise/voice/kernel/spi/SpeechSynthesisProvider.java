package com.rymcu.mortise.voice.kernel.spi;

import com.rymcu.mortise.voice.kernel.model.TtsRequest;
import com.rymcu.mortise.voice.kernel.model.TtsResponse;
import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;

/**
 * 语音合成提供商。
 */
public interface SpeechSynthesisProvider {

    VoiceProviderType getProviderType();

    boolean isAvailable();

    TtsResponse synthesize(TtsRequest request);
}