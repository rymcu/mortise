package com.rymcu.mortise.voice.infra.catalog;

import com.rymcu.mortise.voice.kernel.catalog.VoiceCatalogContribution;
import com.rymcu.mortise.voice.kernel.catalog.VoiceCatalogContributor;
import com.rymcu.mortise.voice.kernel.catalog.VoiceModelDescriptor;
import com.rymcu.mortise.voice.kernel.catalog.VoiceProfileDescriptor;
import com.rymcu.mortise.voice.kernel.catalog.VoiceProviderDescriptor;
import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import com.rymcu.mortise.voice.kernel.model.VoiceCapability;
import com.rymcu.mortise.voice.kernel.model.VoiceModelType;
import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;
import com.rymcu.mortise.voice.kernel.spi.VoiceRuntimeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 基于本地 runtime 节点状态贡献默认 ASR 目录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalRuntimeVoiceCatalogContributor implements VoiceCatalogContributor {

    static final String LOCAL_RUNTIME_PROVIDER_CODE = "local-runtime";
    static final String LOCAL_RUNTIME_PROVIDER_NAME = "Local Runtime";
    static final String DEFAULT_ASR_MODEL_CODE = "sense-voice-zh-en-ja-ko-yue-int8";
    static final String DEFAULT_ASR_MODEL_NAME = "SenseVoice Zh/En/Ja/Ko/Yue Int8";
    static final String DEFAULT_ASR_PROFILE_CODE = "sense-voice-default";
    static final String DEFAULT_ASR_PROFILE_NAME = "SenseVoice Default";
    private static final int STATUS_ENABLED = 1;

    private final VoiceRuntimeClient voiceRuntimeClient;
    private final VoiceProperties voiceProperties;

    @Override
    public List<VoiceCatalogContribution> contribute() {
        if (!hasConfiguredRuntimeNode()) {
            return List.of();
        }
        if (Boolean.TRUE.equals(voiceProperties.catalog().bootstrap().requireHealthyRuntime()) && !hasHealthyRuntimeNode()) {
            return List.of();
        }
        return List.of(buildLocalRuntimeContribution());
    }

    private boolean hasConfiguredRuntimeNode() {
        return voiceProperties.runtime().nodes().stream()
                .anyMatch(node -> node.enabled() && StringUtils.hasText(node.baseUrl()));
    }

    private boolean hasHealthyRuntimeNode() {
        try {
            return voiceRuntimeClient.listNodes().stream()
                    .anyMatch(node -> "healthy".equalsIgnoreCase(node.probeStatus()));
        } catch (Exception exception) {
            log.warn("读取语音 runtime 节点健康状态失败，跳过本地目录贡献", exception);
            return false;
        }
    }

    private VoiceCatalogContribution buildLocalRuntimeContribution() {
        return new VoiceCatalogContribution(
                new VoiceProviderDescriptor(
                        LOCAL_RUNTIME_PROVIDER_CODE,
                        LOCAL_RUNTIME_PROVIDER_NAME,
                        VoiceProviderType.LOCAL_RUNTIME,
                        STATUS_ENABLED,
                        0,
                        null,
                        null
                ),
                List.of(new VoiceModelDescriptor(
                        LOCAL_RUNTIME_PROVIDER_CODE,
                        DEFAULT_ASR_MODEL_CODE,
                        DEFAULT_ASR_MODEL_NAME,
                        VoiceCapability.ASR,
                        VoiceModelType.SENSEVOICE,
                        DEFAULT_ASR_MODEL_CODE,
                        null,
                        null,
                        null,
                        true,
                        STATUS_ENABLED,
                        null
                )),
                List.of(new VoiceProfileDescriptor(
                        DEFAULT_ASR_PROFILE_CODE,
                        DEFAULT_ASR_PROFILE_NAME,
                        null,
                        LOCAL_RUNTIME_PROVIDER_CODE,
                        DEFAULT_ASR_MODEL_CODE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        STATUS_ENABLED,
                        0,
                        null
                ))
        );
    }
}
