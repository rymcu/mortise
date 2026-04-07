package com.rymcu.mortise.voice.application.service.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.application.query.VoiceJobSearchQuery;
import com.rymcu.mortise.voice.application.result.VoiceJobResult;

/**
 * 语音任务查询服务。
 */
public interface VoiceJobQueryService {

    PageResult<VoiceJobResult> findJobs(PageQuery pageQuery, VoiceJobSearchQuery searchQuery);

    VoiceJobResult findJobById(Long id);

    VoiceJobResult findJobByIdForUser(Long id, Long userId);
}