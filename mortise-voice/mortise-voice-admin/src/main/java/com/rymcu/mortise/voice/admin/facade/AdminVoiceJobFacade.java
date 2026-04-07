package com.rymcu.mortise.voice.admin.facade;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.admin.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.admin.contract.response.VoiceJobInfo;

/**
 * 管理端语音任务门面。
 */
public interface AdminVoiceJobFacade {

    PageResult<VoiceJobInfo> findJobPage(PageQuery pageQuery, VoiceJobSearch search);

    VoiceJobInfo findJobById(Long id);
}