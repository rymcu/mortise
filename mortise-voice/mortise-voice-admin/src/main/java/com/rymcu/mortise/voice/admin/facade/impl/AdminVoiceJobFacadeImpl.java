package com.rymcu.mortise.voice.admin.facade.impl;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.admin.assembler.AdminVoiceJobAssembler;
import com.rymcu.mortise.voice.admin.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.admin.contract.response.VoiceJobInfo;
import com.rymcu.mortise.voice.admin.facade.AdminVoiceJobFacade;
import com.rymcu.mortise.voice.application.service.query.VoiceJobQueryService;
import org.springframework.stereotype.Service;

/**
 * 管理端语音任务门面实现。
 */
@Service
public class AdminVoiceJobFacadeImpl implements AdminVoiceJobFacade {

    private final VoiceJobQueryService voiceJobQueryService;
    private final AdminVoiceJobAssembler assembler;

    public AdminVoiceJobFacadeImpl(
            VoiceJobQueryService voiceJobQueryService,
            AdminVoiceJobAssembler assembler
    ) {
        this.voiceJobQueryService = voiceJobQueryService;
        this.assembler = assembler;
    }

    @Override
    public PageResult<VoiceJobInfo> findJobPage(PageQuery pageQuery, VoiceJobSearch search) {
        return voiceJobQueryService.findJobs(pageQuery, assembler.toSearchQuery(search)).map(assembler::toInfo);
    }

    @Override
    public VoiceJobInfo findJobById(Long id) {
        return assembler.toInfo(voiceJobQueryService.findJobById(id));
    }
}