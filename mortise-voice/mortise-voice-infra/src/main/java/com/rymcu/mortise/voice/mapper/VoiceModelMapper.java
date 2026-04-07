package com.rymcu.mortise.voice.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceModelPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 语音模型 Mapper。
 */
@Mapper
public interface VoiceModelMapper extends BaseMapper<VoiceModelPO> {
}