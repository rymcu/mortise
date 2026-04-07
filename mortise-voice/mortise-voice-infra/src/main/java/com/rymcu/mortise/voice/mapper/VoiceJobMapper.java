package com.rymcu.mortise.voice.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceJobPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 语音任务 Mapper。
 */
@Mapper
public interface VoiceJobMapper extends BaseMapper<VoiceJobPO> {
}