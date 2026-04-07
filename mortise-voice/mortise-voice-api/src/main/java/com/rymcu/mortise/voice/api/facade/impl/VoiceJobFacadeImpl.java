package com.rymcu.mortise.voice.api.facade.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.api.assembler.VoiceApiAssembler;
import com.rymcu.mortise.voice.api.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobDetailResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobSummaryResponse;
import com.rymcu.mortise.voice.api.facade.VoiceJobFacade;
import com.rymcu.mortise.voice.application.query.VoiceJobSearchQuery;
import com.rymcu.mortise.voice.application.service.query.VoiceJobQueryService;
import org.springframework.stereotype.Service;

/**
 * 用户端语音任务门面实现。
 */
@Service
public class VoiceJobFacadeImpl implements VoiceJobFacade {

    private final VoiceJobQueryService voiceJobQueryService;
    private final VoiceApiAssembler assembler;

    public VoiceJobFacadeImpl(
            VoiceJobQueryService voiceJobQueryService,
            VoiceApiAssembler assembler
    ) {
        this.voiceJobQueryService = voiceJobQueryService;
        this.assembler = assembler;
    }

    @Override
    public PageResult<VoiceJobSummaryResponse> listJobs(Long userId, PageQuery pageQuery, VoiceJobSearch search) {
        Long currentUserId = requireUserId(userId);
        VoiceJobSearchQuery query = new VoiceJobSearchQuery(
                search != null ? search.getQuery() : null,
                search != null ? search.getJobStatus() : null,
                search != null ? search.getJobType() : null,
                null,
                currentUserId
        );
        return voiceJobQueryService.findJobs(pageQuery, query).map(assembler::toJobSummaryResponse);
    }

    @Override
    public VoiceJobDetailResponse findJobById(Long userId, Long jobId) {
        return assembler.toJobDetailResponse(voiceJobQueryService.findJobByIdForUser(jobId, requireUserId(userId)));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("当前用户不存在");
        }
        return userId;
    }
}