package com.rymcu.mortise.voice.application.service.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.application.query.VoiceModelSearchQuery;
import com.rymcu.mortise.voice.application.query.VoiceProfileSearchQuery;
import com.rymcu.mortise.voice.application.query.VoiceProviderSearchQuery;
import com.rymcu.mortise.voice.application.result.VoiceModelResult;
import com.rymcu.mortise.voice.application.result.VoiceProfileResult;
import com.rymcu.mortise.voice.application.result.VoiceProviderResult;
import com.rymcu.mortise.voice.kernel.model.VoiceRuntimeNodeStatus;

import java.util.List;

/**
 * 语音目录查询服务。
 */
public interface VoiceCatalogQueryService {

    List<VoiceProviderResult> listProviders(Boolean enabledOnly);

    PageResult<VoiceProviderResult> findProviders(PageQuery pageQuery, VoiceProviderSearchQuery searchQuery);

    VoiceProviderResult findProviderById(Long id);

    List<VoiceModelResult> listModels(Boolean enabledOnly);

    PageResult<VoiceModelResult> findModels(PageQuery pageQuery, VoiceModelSearchQuery searchQuery);

    VoiceModelResult findModelById(Long id);

    List<VoiceProfileResult> listProfiles(Boolean enabledOnly);

    PageResult<VoiceProfileResult> findProfiles(PageQuery pageQuery, VoiceProfileSearchQuery searchQuery);

    VoiceProfileResult findProfileById(Long id);

    List<VoiceRuntimeNodeStatus> listRuntimeNodes();
}