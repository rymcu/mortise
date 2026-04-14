package com.rymcu.mortise.voice.kernel.catalog;

import java.util.Objects;

/**
 * 语音配置目录描述。
 */
public record VoiceProfileDescriptor(
        String code,
        String name,
        String language,
        String asrProviderCode,
        String asrModelCode,
        String vadProviderCode,
        String vadModelCode,
        String ttsProviderCode,
        String ttsModelCode,
        String defaultParams,
        Integer status,
        Integer sortNo,
        String remark
) {

    public VoiceProfileDescriptor {
        code = Objects.requireNonNull(code, "code cannot be null").strip();
        name = Objects.requireNonNull(name, "name cannot be null").strip();
        language = language != null ? language.strip() : null;
        asrProviderCode = asrProviderCode != null ? asrProviderCode.strip() : null;
        asrModelCode = asrModelCode != null ? asrModelCode.strip() : null;
        vadProviderCode = vadProviderCode != null ? vadProviderCode.strip() : null;
        vadModelCode = vadModelCode != null ? vadModelCode.strip() : null;
        ttsProviderCode = ttsProviderCode != null ? ttsProviderCode.strip() : null;
        ttsModelCode = ttsModelCode != null ? ttsModelCode.strip() : null;
        defaultParams = defaultParams != null ? defaultParams.strip() : null;
        status = status != null ? status : 1;
        sortNo = sortNo != null ? sortNo : 0;
        remark = remark != null ? remark.strip() : null;
    }
}
