package com.rymcu.mortise.voice.application.service.command.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.voice.application.command.VoiceSynthesizeCommand;
import com.rymcu.mortise.voice.application.result.VoiceSynthesizeResult;
import com.rymcu.mortise.voice.application.service.command.VoiceTtsCommandService;
import com.rymcu.mortise.voice.entity.VoiceArtifact;
import com.rymcu.mortise.voice.entity.VoiceJob;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import com.rymcu.mortise.voice.kernel.model.TtsRequest;
import com.rymcu.mortise.voice.kernel.model.TtsResponse;
import com.rymcu.mortise.voice.kernel.model.VoiceArtifactType;
import com.rymcu.mortise.voice.kernel.model.VoiceJobStatus;
import com.rymcu.mortise.voice.kernel.model.VoiceJobType;
import com.rymcu.mortise.voice.kernel.spi.VoiceRuntimeClient;
import com.rymcu.mortise.voice.repository.VoiceArtifactRepository;
import com.rymcu.mortise.voice.repository.VoiceJobRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.Objects;

/**
 * 同步语音合成命令服务实现。
 */
@Slf4j
@Service
public class VoiceTtsCommandServiceImpl implements VoiceTtsCommandService {

    private static final String DEFAULT_SOURCE_MODULE = "voice-api";
    private static final int MAX_RESULT_SUMMARY_LENGTH = 500;
    private static final String DEFAULT_AUDIO_CONTENT_TYPE = "application/octet-stream";

    private final VoiceProfileRepository voiceProfileRepository;
    private final VoiceArtifactRepository voiceArtifactRepository;
    private final VoiceJobRepository voiceJobRepository;
    private final VoiceRuntimeClient voiceRuntimeClient;
    private final VoiceProperties voiceProperties;
    private final FileStorageService fileStorageService;

    public VoiceTtsCommandServiceImpl(
            VoiceProfileRepository voiceProfileRepository,
            VoiceArtifactRepository voiceArtifactRepository,
            VoiceJobRepository voiceJobRepository,
            VoiceRuntimeClient voiceRuntimeClient,
            VoiceProperties voiceProperties,
            FileStorageService fileStorageService
    ) {
        this.voiceProfileRepository = voiceProfileRepository;
        this.voiceArtifactRepository = voiceArtifactRepository;
        this.voiceJobRepository = voiceJobRepository;
        this.voiceRuntimeClient = voiceRuntimeClient;
        this.voiceProperties = voiceProperties;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public VoiceSynthesizeResult synthesize(VoiceSynthesizeCommand command) {
        validateCommand(command);
        VoiceProfile profile = requireSynthesizableProfile(command.profileCode());

        VoiceJob job = createProcessingJob(profile.getId(), command.userId(), command.sourceModule());
        voiceJobRepository.save(job);

        try {
            TtsResponse response = voiceRuntimeClient.synthesize(new TtsRequest(
                    profile.getCode(),
                    command.text().strip(),
                    normalizeVoiceName(command.voiceName())
            ));
            PersistedArtifact artifact = completeJob(job, response, command.text());
            return new VoiceSynthesizeResult(job.getId(), artifact.artifactId(), response.format(), artifact.fileUrl());
        } catch (BusinessException | ServiceException exception) {
            failJob(job, exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            failJob(job, exception.getMessage());
            throw new ServiceException("同步语音合成失败: " + exception.getMessage(), exception);
        }
    }

    private void validateCommand(VoiceSynthesizeCommand command) {
        if (command == null) {
            throw new BusinessException("语音合成请求不能为空");
        }
        if (!StringUtils.hasText(command.profileCode())) {
            throw new BusinessException("语音配置编码不能为空");
        }
        if (!StringUtils.hasText(command.text())) {
            throw new BusinessException("待合成文本不能为空");
        }
        if (command.text().strip().length() > voiceProperties.tts().maxTextLength()) {
            throw new BusinessException("待合成文本长度超过限制");
        }
    }

    private VoiceProfile requireSynthesizableProfile(String profileCode) {
        VoiceProfile profile = voiceProfileRepository.findByCode(profileCode.strip())
                .orElseThrow(() -> new BusinessException("语音配置不存在"));
        if (!Objects.equals(profile.getStatus(), Status.ENABLED.getCode())) {
            throw new BusinessException("语音配置未启用");
        }
        if (profile.getTtsProviderId() == null || profile.getTtsModelId() == null) {
            throw new BusinessException("语音配置未绑定 TTS Provider/Model");
        }
        return profile;
    }

    private VoiceJob createProcessingJob(Long profileId, Long userId, String sourceModule) {
        VoiceJob job = new VoiceJob();
        job.setJobType(VoiceJobType.TTS_SYNC.name());
        job.setStatus(VoiceJobStatus.PROCESSING.name());
        job.setProfileId(profileId);
        job.setUserId(userId);
        job.setSourceModule(StringUtils.hasText(sourceModule) ? sourceModule.strip() : DEFAULT_SOURCE_MODULE);
        return job;
    }

    private PersistedArtifact completeJob(VoiceJob job, TtsResponse response, String sourceText) {
        PersistedArtifact artifact = persistAudioArtifact(job, response);
        job.setStatus(VoiceJobStatus.COMPLETED.name());
        job.setResultSummary(trimSummary(sourceText));
        job.setErrorMessage(null);
        voiceJobRepository.update(job);
        return artifact;
    }

    private void failJob(VoiceJob job, String message) {
        job.setStatus(VoiceJobStatus.FAILED.name());
        job.setErrorMessage(trimSummary(message));
        voiceJobRepository.update(job);
    }

    private PersistedArtifact persistAudioArtifact(VoiceJob job, TtsResponse response) {
        if (job.getId() == null) {
            return new PersistedArtifact(null, response.downloadUrl());
        }
        byte[] content = response.content();
        if (content != null && content.length > 0) {
            String contentType = normalizeContentType(response.contentType());
            String extension = resolveExtension(response.format(), contentType);
            String filename = "voice-tts-" + job.getId() + extension;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
                FileInfo fileInfo = fileStorageService.of(inputStream, filename, contentType, (long) content.length)
                        .setObjectId(String.valueOf(job.getId()))
                        .setObjectType("voice_artifact")
                        .upload();
                VoiceArtifact artifact = new VoiceArtifact();
                artifact.setJobId(job.getId());
                artifact.setFileId(parseFileId(fileInfo));
                artifact.setArtifactType(VoiceArtifactType.TTS_AUDIO.name());
                artifact.setContentType(contentType);
                artifact.setBucket(fileInfo.getBasePath());
                artifact.setObjectKey(fileInfo.getPath());
                voiceArtifactRepository.save(artifact);
                return new PersistedArtifact(artifact.getId(), fileInfo.getUrl());
            } catch (Exception exception) {
                log.warn("保存语音合成音频产物失败, jobId={}", job.getId(), exception);
            }
        }
        if (StringUtils.hasText(response.downloadUrl())) {
            VoiceArtifact artifact = new VoiceArtifact();
            artifact.setJobId(job.getId());
            artifact.setArtifactType(VoiceArtifactType.TTS_AUDIO.name());
            artifact.setContentType(normalizeContentType(response.contentType()));
            artifact.setObjectKey(response.downloadUrl().strip());
            voiceArtifactRepository.save(artifact);
            return new PersistedArtifact(artifact.getId(), response.downloadUrl().strip());
        }
        return new PersistedArtifact(null, null);
    }

    private Long parseFileId(FileInfo fileInfo) {
        if (fileInfo == null || !StringUtils.hasText(fileInfo.getId())) {
            return null;
        }
        try {
            return Long.parseLong(fileInfo.getId());
        } catch (NumberFormatException exception) {
            log.warn("无法解析语音合成产物 fileId: {}", fileInfo.getId());
            return null;
        }
    }

    private String normalizeVoiceName(String voiceName) {
        return StringUtils.hasText(voiceName) ? voiceName.strip() : null;
    }

    private String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType.strip() : DEFAULT_AUDIO_CONTENT_TYPE;
    }

    private String resolveExtension(String format, String contentType) {
        if (StringUtils.hasText(format)) {
            return "." + format.strip().toLowerCase(Locale.ROOT);
        }
        if (StringUtils.hasText(contentType) && contentType.contains("/")) {
            String subtype = contentType.substring(contentType.indexOf('/') + 1);
            int separator = subtype.indexOf(';');
            if (separator >= 0) {
                subtype = subtype.substring(0, separator);
            }
            if (StringUtils.hasText(subtype)) {
                return "." + subtype.strip().toLowerCase(Locale.ROOT);
            }
        }
        return ".bin";
    }

    private String trimSummary(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.length() <= MAX_RESULT_SUMMARY_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_RESULT_SUMMARY_LENGTH);
    }

    private record PersistedArtifact(
            Long artifactId,
            String fileUrl
    ) {
    }
}