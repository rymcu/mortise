package com.rymcu.mortise.voice.kernel.catalog;

import java.util.List;
import java.util.Objects;

/**
 * 一组应同步到后台目录的系统默认项。
 */
public record VoiceCatalogContribution(
        VoiceProviderDescriptor provider,
        List<VoiceModelDescriptor> models,
        List<VoiceProfileDescriptor> profiles
) {

    public VoiceCatalogContribution {
        provider = Objects.requireNonNull(provider, "provider cannot be null");
        models = models != null ? List.copyOf(models) : List.of();
        profiles = profiles != null ? List.copyOf(profiles) : List.of();
    }
}
