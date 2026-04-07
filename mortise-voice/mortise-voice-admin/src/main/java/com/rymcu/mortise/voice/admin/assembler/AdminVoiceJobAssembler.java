package com.rymcu.mortise.voice.admin.assembler;

import com.rymcu.mortise.voice.admin.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.admin.contract.response.VoiceArtifactInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceJobInfo;
import com.rymcu.mortise.voice.application.query.VoiceJobSearchQuery;
import com.rymcu.mortise.voice.application.result.VoiceArtifactResult;
import com.rymcu.mortise.voice.application.result.VoiceJobResult;
import org.springframework.stereotype.Component;

/**
 * 管理端语音任务转换器。
 */
@Component
public class AdminVoiceJobAssembler {

    public VoiceJobSearchQuery toSearchQuery(VoiceJobSearch search) {
        if (search == null) {
            return null;
        }
        return new VoiceJobSearchQuery(
                search.getQuery(),
            search.getJobStatus(),
                search.getJobType(),
            search.getProfileId(),
            null
        );
    }

    public VoiceJobInfo toInfo(VoiceJobResult result) {
        return new VoiceJobInfo(
                result.id(),
                result.jobType(),
                result.status(),
                result.profileId(),
                result.profileName(),
                result.profileCode(),
                result.userId(),
                result.sourceModule(),
                result.durationMillis(),
                result.resultSummary(),
                result.errorMessage(),
                result.artifacts() == null ? null : result.artifacts().stream().map(this::toInfo).toList(),
                result.createdTime(),
                result.updatedTime()
        );
    }

    public VoiceArtifactInfo toInfo(VoiceArtifactResult result) {
        return new VoiceArtifactInfo(
                result.id(),
                result.fileId(),
                result.artifactType(),
                result.contentType(),
                result.bucket(),
                result.objectKey(),
                result.fileUrl(),
                result.filename(),
                result.originalFilename(),
                result.createdTime()
        );
    }
}