package com.rymcu.mortise.voice.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceProfilePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 语音配置 Mapper。
 */
@Mapper
public interface VoiceProfileMapper extends BaseMapper<VoiceProfilePO> {
}