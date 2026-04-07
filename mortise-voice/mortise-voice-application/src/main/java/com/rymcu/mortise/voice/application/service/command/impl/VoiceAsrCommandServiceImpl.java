package com.rymcu.mortise.voice.application.service.command.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.voice.application.command.VoiceRecognizeOnceCommand;
import com.rymcu.mortise.voice.application.result.VoiceRecognizeOnceResult;
import com.rymcu.mortise.voice.application.service.command.VoiceAsrCommandService;
import com.rymcu.mortise.voice.entity.VoiceArtifact;
import com.rymcu.mortise.voice.entity.VoiceJob;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import com.rymcu.mortise.voice.kernel.model.AsrRequest;
import com.rymcu.mortise.voice.kernel.model.AsrResponse;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 短音频识别命令服务实现。
 */
@Slf4j
@Service
public class VoiceAsrCommandServiceImpl implements VoiceAsrCommandService {

    private static final String DEFAULT_SOURCE_MODULE = "voice-api";
    private static final int MAX_RESULT_SUMMARY_LENGTH = 500;
    private static final String TRANSCRIPT_CONTENT_TYPE = "text/plain;charset=UTF-8";
    private static final String ARTIFACT_OBJECT_TYPE = "voice_artifact";

    private final VoiceProfileRepository voiceProfileRepository;
    private final VoiceArtifactRepository voiceArtifactRepository;
    private final VoiceJobRepository voiceJobRepository;
    private final VoiceRuntimeClient voiceRuntimeClient;
    private final VoiceProperties voiceProperties;
    private final FileStorageService fileStorageService;

    public VoiceAsrCommandServiceImpl(
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
    public VoiceRecognizeOnceResult recognizeOnce(VoiceRecognizeOnceCommand command) {
        validateCommand(command);
        VoiceProfile profile = requireRecognizableProfile(command.profileCode());

        VoiceJob job = createProcessingJob(profile.getId(), command.userId(), command.sourceModule());
        voiceJobRepository.save(job);

        try {
            AsrResponse response = voiceRuntimeClient.recognizeOnce(new AsrRequest(
                    profile.getCode(),
                    normalizeFileName(command.fileName()),
                    normalizeContentType(command.contentType()),
                    command.size(),
                    command.content()
            ));
            completeJob(job, response);
            return new VoiceRecognizeOnceResult(
                    job.getId(),
                    response.text(),
                    response.language(),
                    response.durationSeconds(),
                    response.tokens() != null ? response.tokens() : List.of(),
                    response.timestamps() != null ? response.timestamps() : List.of()
            );
        } catch (BusinessException | ServiceException exception) {
            failJob(job, exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            failJob(job, exception.getMessage());
            throw new ServiceException("短音频识别失败: " + exception.getMessage(), exception);
        }
    }

    private void validateCommand(VoiceRecognizeOnceCommand command) {
        if (command == null) {
            throw new BusinessException("识别请求不能为空");
        }
        if (!StringUtils.hasText(command.profileCode())) {
            throw new BusinessException("语音配置编码不能为空");
        }
        if (command.content() == null || command.content().length == 0 || command.size() <= 0) {
            throw new BusinessException("音频文件不能为空");
        }
        if (command.size() > voiceProperties.asr().maxFileSize()) {
            throw new BusinessException("音频文件大小超过限制");
        }
    }

    private VoiceProfile requireRecognizableProfile(String profileCode) {
        VoiceProfile profile = voiceProfileRepository.findByCode(profileCode.strip())
                .orElseThrow(() -> new BusinessException("语音配置不存在"));
        if (!Objects.equals(profile.getStatus(), Status.ENABLED.getCode())) {
            throw new BusinessException("语音配置未启用");
        }
        if (profile.getAsrProviderId() == null || profile.getAsrModelId() == null) {
            throw new BusinessException("语音配置未绑定 ASR Provider/Model");
        }
        return profile;
    }

    private VoiceJob createProcessingJob(Long profileId, Long userId, String sourceModule) {
        VoiceJob job = new VoiceJob();
        job.setJobType(VoiceJobType.ASR_SYNC.name());
        job.setStatus(VoiceJobStatus.PROCESSING.name());
        job.setProfileId(profileId);
        job.setUserId(userId);
        job.setSourceModule(StringUtils.hasText(sourceModule) ? sourceModule.strip() : DEFAULT_SOURCE_MODULE);
        return job;
    }

    private void completeJob(VoiceJob job, AsrResponse response) {
        job.setStatus(VoiceJobStatus.COMPLETED.name());
        job.setDurationMillis(toDurationMillis(response.durationSeconds()));
        job.setResultSummary(trimSummary(response.text()));
        job.setErrorMessage(null);
        voiceJobRepository.update(job);
        persistTranscriptArtifact(job, response.text());
    }

    private void failJob(VoiceJob job, String message) {
        job.setStatus(VoiceJobStatus.FAILED.name());
        job.setErrorMessage(trimSummary(message));
        voiceJobRepository.update(job);
    }

    private String normalizeFileName(String fileName) {
        return StringUtils.hasText(fileName) ? fileName.strip() : "audio.bin";
    }

    private String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType.strip() : "application/octet-stream";
    }

    private Long toDurationMillis(Double durationSeconds) {
        if (durationSeconds == null) {
            return null;
        }
        return Math.round(durationSeconds * 1000);
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

    private void persistTranscriptArtifact(VoiceJob job, String transcript) {
        if (job.getId() == null || !StringUtils.hasText(transcript)) {
            return;
        }

        byte[] content = transcript.strip().getBytes(StandardCharsets.UTF_8);
        String filename = "voice-asr-" + job.getId() + ".txt";
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            FileInfo fileInfo = fileStorageService.of(inputStream, filename, TRANSCRIPT_CONTENT_TYPE, (long) content.length)
                    .setObjectId(String.valueOf(job.getId()))
                    .setObjectType(ARTIFACT_OBJECT_TYPE)
                    .upload();
            VoiceArtifact artifact = new VoiceArtifact();
            artifact.setJobId(job.getId());
            artifact.setFileId(parseFileId(fileInfo));
            artifact.setArtifactType(VoiceArtifactType.ASR_TRANSCRIPT.name());
            artifact.setContentType(TRANSCRIPT_CONTENT_TYPE);
            artifact.setBucket(fileInfo.getBasePath());
            artifact.setObjectKey(fileInfo.getPath());
            voiceArtifactRepository.save(artifact);
        } catch (Exception exception) {
            log.warn("保存语音识别文本产物失败, jobId={}", job.getId(), exception);
        }
    }

    private Long parseFileId(FileInfo fileInfo) {
        if (fileInfo == null || !StringUtils.hasText(fileInfo.getId())) {
            return null;
        }
        try {
            return Long.parseLong(fileInfo.getId());
        } catch (NumberFormatException exception) {
            log.warn("无法解析语音产物 fileId: {}", fileInfo.getId());
            return null;
        }
    }
}