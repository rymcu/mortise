package com.rymcu.mortise.voice.kernel.provider;

import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;
import com.rymcu.mortise.voice.kernel.spi.SpeechRecognitionProvider;
import com.rymcu.mortise.voice.kernel.spi.SpeechSynthesisProvider;
import com.rymcu.mortise.voice.kernel.spi.VadProvider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音提供商注册中心。
 */
public class VoiceProviderRegistry {

    private final Map<VoiceProviderType, SpeechRecognitionProvider> recognitionProviders = new ConcurrentHashMap<>();
    private final Map<VoiceProviderType, SpeechSynthesisProvider> synthesisProviders = new ConcurrentHashMap<>();
    private final Map<VoiceProviderType, VadProvider> vadProviders = new ConcurrentHashMap<>();

    public void register(SpeechRecognitionProvider provider) {
        recognitionProviders.put(provider.getProviderType(), provider);
    }

    public void register(SpeechSynthesisProvider provider) {
        synthesisProviders.put(provider.getProviderType(), provider);
    }

    public void register(VadProvider provider) {
        vadProviders.put(provider.getProviderType(), provider);
    }

    public Optional<SpeechRecognitionProvider> getRecognitionProvider(VoiceProviderType providerType) {
        return Optional.ofNullable(recognitionProviders.get(providerType));
    }

    public Optional<SpeechSynthesisProvider> getSynthesisProvider(VoiceProviderType providerType) {
        return Optional.ofNullable(synthesisProviders.get(providerType));
    }

    public Optional<VadProvider> getVadProvider(VoiceProviderType providerType) {
        return Optional.ofNullable(vadProviders.get(providerType));
    }
}