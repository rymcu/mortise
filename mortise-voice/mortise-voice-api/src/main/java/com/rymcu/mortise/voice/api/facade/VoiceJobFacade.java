package com.rymcu.mortise.voice.api.facade;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.api.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobDetailResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobSummaryResponse;

/**
 * 用户端语音任务门面。
 */
public interface VoiceJobFacade {

    PageResult<VoiceJobSummaryResponse> listJobs(Long userId, PageQuery pageQuery, VoiceJobSearch search);

    VoiceJobDetailResponse findJobById(Long userId, Long jobId);
}