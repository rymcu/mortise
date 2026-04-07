package com.rymcu.mortise.voice.application.service.query.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.file.entity.FileDetail;
import com.rymcu.mortise.file.mapper.FileDetailMapper;
import com.rymcu.mortise.voice.application.result.VoiceArtifactResult;
import com.rymcu.mortise.voice.application.query.VoiceJobSearchQuery;
import com.rymcu.mortise.voice.application.result.VoiceJobResult;
import com.rymcu.mortise.voice.application.service.query.VoiceJobQueryService;
import com.rymcu.mortise.voice.entity.VoiceArtifact;
import com.rymcu.mortise.voice.entity.VoiceJob;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.model.VoiceJobSearchCriteria;
import com.rymcu.mortise.voice.repository.VoiceArtifactRepository;
import com.rymcu.mortise.voice.repository.VoiceJobRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 语音任务查询服务实现。
 */
@Service
public class VoiceJobQueryServiceImpl implements VoiceJobQueryService {

    private final VoiceJobRepository voiceJobRepository;
    private final VoiceArtifactRepository voiceArtifactRepository;
    private final VoiceProfileRepository voiceProfileRepository;
    private final FileDetailMapper fileDetailMapper;

    public VoiceJobQueryServiceImpl(
            VoiceJobRepository voiceJobRepository,
            VoiceArtifactRepository voiceArtifactRepository,
            VoiceProfileRepository voiceProfileRepository,
            FileDetailMapper fileDetailMapper
    ) {
        this.voiceJobRepository = voiceJobRepository;
        this.voiceArtifactRepository = voiceArtifactRepository;
        this.voiceProfileRepository = voiceProfileRepository;
        this.fileDetailMapper = fileDetailMapper;
    }

    @Override
    public PageResult<VoiceJobResult> findJobs(PageQuery pageQuery, VoiceJobSearchQuery searchQuery) {
        return voiceJobRepository.findJobs(pageQuery, toCriteria(searchQuery)).map(this::toSummaryResult);
    }

    @Override
    public VoiceJobResult findJobById(Long id) {
        return voiceJobRepository.findById(id)
                .map(this::toDetailResult)
                .orElseThrow(() -> new BusinessException("语音任务不存在"));
    }

    @Override
    public VoiceJobResult findJobByIdForUser(Long id, Long userId) {
        return voiceJobRepository.findById(id)
                .filter(job -> userId != null && userId.equals(job.getUserId()))
                .map(this::toDetailResult)
                .orElseThrow(() -> new BusinessException("语音任务不存在"));
    }

    private VoiceJobSearchCriteria toCriteria(VoiceJobSearchQuery searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        return new VoiceJobSearchCriteria(
                normalizeQuery(searchQuery.query()),
                normalizeEnumText(searchQuery.status()),
                normalizeEnumText(searchQuery.jobType()),
            searchQuery.profileId(),
            searchQuery.userId()
        );
    }

    private VoiceJobResult toSummaryResult(VoiceJob job) {
        return buildResult(job, List.of());
    }

    private VoiceJobResult toDetailResult(VoiceJob job) {
        return buildResult(job, listArtifacts(job.getId()));
    }

    private VoiceJobResult buildResult(VoiceJob job, List<VoiceArtifactResult> artifacts) {
        VoiceProfile profile = null;
        if (job.getProfileId() != null) {
            profile = voiceProfileRepository.findById(job.getProfileId()).orElse(null);
        }
        return new VoiceJobResult(
                job.getId(),
                job.getJobType(),
                job.getStatus(),
                job.getProfileId(),
                profile == null ? null : profile.getName(),
                profile == null ? null : profile.getCode(),
                job.getUserId(),
                job.getSourceModule(),
                job.getDurationMillis(),
                job.getResultSummary(),
                job.getErrorMessage(),
                artifacts,
                job.getCreatedTime(),
                job.getUpdatedTime()
        );
    }

    private List<VoiceArtifactResult> listArtifacts(Long jobId) {
        if (jobId == null) {
            return List.of();
        }
        return voiceArtifactRepository.findByJobId(jobId).stream()
                .map(this::toArtifactResult)
                .toList();
    }

    private VoiceArtifactResult toArtifactResult(VoiceArtifact artifact) {
        FileDetail fileDetail = artifact.getFileId() == null ? null : fileDetailMapper.selectOneById(artifact.getFileId());
        String fileUrl = fileDetail == null ? null : fileDetail.getUrl();
        if (!StringUtils.hasText(fileUrl) && StringUtils.hasText(artifact.getObjectKey())
            && (artifact.getObjectKey().startsWith("http://") || artifact.getObjectKey().startsWith("https://"))) {
            fileUrl = artifact.getObjectKey();
        }
        return new VoiceArtifactResult(
                artifact.getId(),
                artifact.getFileId(),
                artifact.getArtifactType(),
                artifact.getContentType(),
                artifact.getBucket(),
                artifact.getObjectKey(),
            fileUrl,
                fileDetail == null ? null : fileDetail.getFilename(),
                fileDetail == null ? null : fileDetail.getOriginalFilename(),
                artifact.getCreatedTime()
        );
    }

    private String normalizeQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.strip();
    }

    private String normalizeEnumText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.strip().toUpperCase();
    }
}