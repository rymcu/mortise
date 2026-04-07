package com.rymcu.mortise.voice.kernel.spi;

import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;

/**
 * VAD 提供商。
 */
public interface VadProvider {

    VoiceProviderType getProviderType();

    boolean isAvailable();
}