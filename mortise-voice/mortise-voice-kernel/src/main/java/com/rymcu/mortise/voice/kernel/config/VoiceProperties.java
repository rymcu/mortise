package com.rymcu.mortise.voice.kernel.config;

import com.rymcu.mortise.voice.kernel.constant.VoiceConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 语音模块配置。
 */
@ConfigurationProperties(prefix = "mortise.voice")
public record VoiceProperties(
        RuntimeConfig runtime,
        CatalogConfig catalog,
        AsrConfig asr,
        VadConfig vad,
        TtsConfig tts,
        StorageConfig storage,
        RateLimitConfig rateLimit
) {

    public VoiceProperties {
        runtime = runtime != null ? runtime : new RuntimeConfig(List.of(), VoiceConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS, VoiceConstants.DEFAULT_READ_TIMEOUT_MILLIS);
        catalog = catalog != null ? catalog : new CatalogConfig(new CatalogBootstrapConfig(Boolean.TRUE, Boolean.TRUE));
        asr = asr != null ? asr : new AsrConfig(null, null, "/asr/recognize-once", VoiceConstants.DEFAULT_MAX_ASR_FILE_SIZE, VoiceConstants.DEFAULT_MAX_ASR_DURATION_SECONDS);
        vad = vad != null ? vad : new VadConfig(null, 0.5D, 0.25D, 0.5D);
        tts = tts != null ? tts : new TtsConfig(null, null, "/tts/synthesize", VoiceConstants.DEFAULT_MAX_TTS_TEXT_LENGTH);
        storage = storage != null ? storage : new StorageConfig("voice", 30, true);
        rateLimit = rateLimit != null ? rateLimit : new RateLimitConfig("voice-asr", "voice-tts", "voice-ws");
    }

    public record RuntimeConfig(
            List<RuntimeNode> nodes,
            int connectTimeoutMillis,
            int readTimeoutMillis
    ) {
        public RuntimeConfig {
            nodes = nodes != null ? List.copyOf(nodes) : List.of();
            connectTimeoutMillis = connectTimeoutMillis > 0 ? connectTimeoutMillis : VoiceConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS;
            readTimeoutMillis = readTimeoutMillis > 0 ? readTimeoutMillis : VoiceConstants.DEFAULT_READ_TIMEOUT_MILLIS;
        }
    }

    public record CatalogConfig(
            CatalogBootstrapConfig bootstrap
    ) {
        public CatalogConfig {
            bootstrap = bootstrap != null ? bootstrap : new CatalogBootstrapConfig(Boolean.TRUE, Boolean.TRUE);
        }
    }

    public record CatalogBootstrapConfig(
            Boolean enabled,
            Boolean requireHealthyRuntime
    ) {
        public CatalogBootstrapConfig {
            enabled = enabled != null ? enabled : Boolean.TRUE;
            requireHealthyRuntime = requireHealthyRuntime != null ? requireHealthyRuntime : Boolean.TRUE;
        }
    }

    public record RuntimeNode(
            String nodeId,
            String baseUrl,
            boolean enabled,
            List<String> prewarmModels
    ) {
        public RuntimeNode {
            prewarmModels = prewarmModels != null ? List.copyOf(prewarmModels) : List.of();
        }
    }

    public record AsrConfig(
            String defaultProvider,
            String defaultModel,
            String recognizePath,
            long maxFileSize,
            int maxDurationSeconds
    ) {
        public AsrConfig {
            recognizePath = recognizePath != null && !recognizePath.isBlank() ? recognizePath : "/asr/recognize-once";
            maxFileSize = maxFileSize > 0 ? maxFileSize : VoiceConstants.DEFAULT_MAX_ASR_FILE_SIZE;
            maxDurationSeconds = maxDurationSeconds > 0 ? maxDurationSeconds : VoiceConstants.DEFAULT_MAX_ASR_DURATION_SECONDS;
        }
    }

    public record VadConfig(
            String defaultModel,
            double threshold,
            double minSpeechDuration,
            double minSilenceDuration
    ) {
    }

    public record TtsConfig(
            String defaultProvider,
            String defaultProfile,
            String synthesizePath,
            int maxTextLength
    ) {
        public TtsConfig {
            synthesizePath = synthesizePath != null && !synthesizePath.isBlank() ? synthesizePath : "/tts/synthesize";
            maxTextLength = maxTextLength > 0 ? maxTextLength : VoiceConstants.DEFAULT_MAX_TTS_TEXT_LENGTH;
        }
    }

    public record StorageConfig(
            String bucket,
            int retentionDays,
            boolean retainRawAudio
    ) {
        public StorageConfig {
            bucket = bucket != null ? bucket : "voice";
            retentionDays = retentionDays > 0 ? retentionDays : 30;
        }
    }

    public record RateLimitConfig(
            String asrLimiter,
            String ttsLimiter,
            String wsLimiter
    ) {
    }
}
