package com.rymcu.mortise.voice.repository;

import com.rymcu.mortise.voice.entity.VoiceArtifact;

import java.util.List;

/**
 * 语音产物仓储端口。
 */
public interface VoiceArtifactRepository {

    List<VoiceArtifact> findByJobId(Long jobId);

    boolean save(VoiceArtifact artifact);
}