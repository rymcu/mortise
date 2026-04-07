package com.rymcu.mortise.voice.repository;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.entity.VoiceJob;
import com.rymcu.mortise.voice.model.VoiceJobSearchCriteria;

import java.util.Optional;

/**
 * 语音任务仓储端口。
 */
public interface VoiceJobRepository {

    PageResult<VoiceJob> findJobs(PageQuery pageQuery, VoiceJobSearchCriteria criteria);

    Optional<VoiceJob> findById(Long id);

    boolean save(VoiceJob job);

    boolean update(VoiceJob job);
}