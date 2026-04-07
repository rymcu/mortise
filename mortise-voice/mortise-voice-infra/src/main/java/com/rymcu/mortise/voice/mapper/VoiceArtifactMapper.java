package com.rymcu.mortise.voice.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceArtifactPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 语音产物 Mapper。
 */
@Mapper
public interface VoiceArtifactMapper extends BaseMapper<VoiceArtifactPO> {
}