package com.rymcu.mortise.voice.api.assembler;

import com.rymcu.mortise.voice.api.contract.response.VoiceArtifactResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceAsrRecognizeResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobDetailResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobSummaryResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceProfileOption;
import com.rymcu.mortise.voice.api.contract.response.VoiceTtsSynthesizeResponse;
import com.rymcu.mortise.voice.application.result.VoiceArtifactResult;
import com.rymcu.mortise.voice.application.result.VoiceJobResult;
import com.rymcu.mortise.voice.application.result.VoiceProfileResult;
import com.rymcu.mortise.voice.application.result.VoiceRecognizeOnceResult;
import com.rymcu.mortise.voice.application.result.VoiceSynthesizeResult;
import org.springframework.stereotype.Component;

/**
 * 用户端语音 DTO 转换器。
 */
@Component
public class VoiceApiAssembler {

    public VoiceProfileOption toProfileOption(VoiceProfileResult result) {
        boolean defaultProfile = result.sortNo() != null && result.sortNo() == 0;
        return new VoiceProfileOption(
                result.code(),
                result.name(),
                result.language(),
                defaultProfile
        );
    }

    public VoiceAsrRecognizeResponse toRecognizeResponse(VoiceRecognizeOnceResult result) {
        return new VoiceAsrRecognizeResponse(
                result.jobId(),
                result.text(),
                result.language(),
                result.durationSeconds(),
                result.tokens(),
                result.timestamps()
        );
    }

    public VoiceTtsSynthesizeResponse toTtsResponse(VoiceSynthesizeResult result) {
        return new VoiceTtsSynthesizeResponse(
                result.jobId(),
                result.artifactId(),
                result.format(),
                result.downloadUrl()
        );
    }

    public VoiceJobSummaryResponse toJobSummaryResponse(VoiceJobResult result) {
        return new VoiceJobSummaryResponse(
                result.id(),
                result.jobType(),
                result.status(),
                result.profileCode(),
                result.profileName(),
                result.durationMillis(),
                result.resultSummary(),
                result.errorMessage(),
                result.createdTime(),
                result.updatedTime()
        );
    }

    public VoiceJobDetailResponse toJobDetailResponse(VoiceJobResult result) {
        return new VoiceJobDetailResponse(
                result.id(),
                result.jobType(),
                result.status(),
                result.profileCode(),
                result.profileName(),
                result.durationMillis(),
                result.resultSummary(),
                result.errorMessage(),
                result.artifacts() == null ? null : result.artifacts().stream().map(this::toArtifactResponse).toList(),
                result.createdTime(),
                result.updatedTime()
        );
    }

    public VoiceArtifactResponse toArtifactResponse(VoiceArtifactResult result) {
        return new VoiceArtifactResponse(
                result.id(),
                result.artifactType(),
                result.fileUrl(),
                result.filename(),
                result.originalFilename(),
                result.contentType(),
                result.createdTime()
        );
    }
}