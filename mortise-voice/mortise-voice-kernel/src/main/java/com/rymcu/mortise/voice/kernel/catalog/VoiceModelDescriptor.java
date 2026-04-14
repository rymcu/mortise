package com.rymcu.mortise.voice.kernel.catalog;

import com.rymcu.mortise.voice.kernel.model.VoiceCapability;
import com.rymcu.mortise.voice.kernel.model.VoiceModelType;

import java.util.Objects;

/**
 * 语音模型目录描述。
 */
public record VoiceModelDescriptor(
        String providerCode,
        String code,
        String name,
        VoiceCapability capability,
        VoiceModelType modelType,
        String runtimeName,
        String version,
        String language,
        Integer concurrencyLimit,
        boolean defaultModel,
        Integer status,
        String remark
) {

    public VoiceModelDescriptor {
        providerCode = Objects.requireNonNull(providerCode, "providerCode cannot be null").strip();
        code = Objects.requireNonNull(code, "code cannot be null").strip();
        name = Objects.requireNonNull(name, "name cannot be null").strip();
        capability = Objects.requireNonNull(capability, "capability cannot be null");
        modelType = Objects.requireNonNull(modelType, "modelType cannot be null");
        runtimeName = runtimeName != null ? runtimeName.strip() : null;
        version = version != null ? version.strip() : null;
        language = language != null ? language.strip() : null;
        status = status != null ? status : 1;
    }
}
