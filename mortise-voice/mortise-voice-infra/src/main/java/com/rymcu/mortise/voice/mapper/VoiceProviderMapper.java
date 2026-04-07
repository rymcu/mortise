package com.rymcu.mortise.voice.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceProviderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 语音提供商 Mapper。
 */
@Mapper
public interface VoiceProviderMapper extends BaseMapper<VoiceProviderPO> {
}