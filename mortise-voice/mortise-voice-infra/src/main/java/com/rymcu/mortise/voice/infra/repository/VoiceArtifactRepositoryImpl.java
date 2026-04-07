package com.rymcu.mortise.voice.infra.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.voice.entity.VoiceArtifact;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceArtifactPO;
import com.rymcu.mortise.voice.mapper.VoiceArtifactMapper;
import com.rymcu.mortise.voice.repository.VoiceArtifactRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.voice.infra.persistence.entity.table.VoiceArtifactPOTableDef.VOICE_ARTIFACT_PO;

/**
 * 语音产物仓储实现。
 */
@Repository
public class VoiceArtifactRepositoryImpl implements VoiceArtifactRepository {

    private final VoiceArtifactMapper voiceArtifactMapper;

    public VoiceArtifactRepositoryImpl(VoiceArtifactMapper voiceArtifactMapper) {
        this.voiceArtifactMapper = voiceArtifactMapper;
    }

    @Override
    public List<VoiceArtifact> findByJobId(Long jobId) {
        if (jobId == null) {
            return List.of();
        }
        return voiceArtifactMapper.selectListByQuery(QueryWrapper.create()
                        .where(VOICE_ARTIFACT_PO.JOB_ID.eq(jobId))
                        .orderBy(VOICE_ARTIFACT_PO.ID.asc()))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean save(VoiceArtifact artifact) {
        VoiceArtifactPO artifactPO = toPersistence(artifact);
        boolean saved = voiceArtifactMapper.insertSelective(artifactPO) > 0;
        if (saved) {
            artifact.setId(artifactPO.getId());
        }
        return saved;
    }

    private VoiceArtifactPO toPersistence(VoiceArtifact artifact) {
        VoiceArtifactPO artifactPO = new VoiceArtifactPO();
        artifactPO.setId(artifact.getId());
        artifactPO.setJobId(artifact.getJobId());
        artifactPO.setFileId(artifact.getFileId());
        artifactPO.setArtifactType(artifact.getArtifactType());
        artifactPO.setContentType(artifact.getContentType());
        artifactPO.setBucket(artifact.getBucket());
        artifactPO.setObjectKey(artifact.getObjectKey());
        artifactPO.setDelFlag(artifact.getDelFlag());
        return artifactPO;
    }

    private VoiceArtifact toDomain(VoiceArtifactPO po) {
        VoiceArtifact artifact = new VoiceArtifact();
        artifact.setId(po.getId());
        artifact.setJobId(po.getJobId());
        artifact.setFileId(po.getFileId());
        artifact.setArtifactType(po.getArtifactType());
        artifact.setContentType(po.getContentType());
        artifact.setBucket(po.getBucket());
        artifact.setObjectKey(po.getObjectKey());
        artifact.setDelFlag(po.getDelFlag());
        artifact.setCreatedTime(po.getCreatedTime());
        return artifact;
    }
}