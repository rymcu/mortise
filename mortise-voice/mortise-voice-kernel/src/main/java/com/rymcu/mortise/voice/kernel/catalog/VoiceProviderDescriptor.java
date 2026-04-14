package com.rymcu.mortise.voice.kernel.catalog;

import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;

import java.util.Objects;

/**
 * 语音提供商目录描述。
 */
public record VoiceProviderDescriptor(
        String code,
        String name,
        VoiceProviderType providerType,
        Integer status,
        Integer sortNo,
        String defaultConfig,
        String remark
) {

    public VoiceProviderDescriptor {
        code = Objects.requireNonNull(code, "code cannot be null").strip();
        name = Objects.requireNonNull(name, "name cannot be null").strip();
        providerType = Objects.requireNonNull(providerType, "providerType cannot be null");
        status = status != null ? status : 1;
        sortNo = sortNo != null ? sortNo : 0;
    }
}
