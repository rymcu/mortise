package com.rymcu.mortise.voice.admin.facade;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.admin.contract.query.VoiceModelSearch;
import com.rymcu.mortise.voice.admin.contract.query.VoiceProfileSearch;
import com.rymcu.mortise.voice.admin.contract.query.VoiceProviderSearch;
import com.rymcu.mortise.voice.admin.contract.request.VoiceModelUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceProfileUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceProviderUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.response.VoiceModelInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceProfileInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceProviderInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceRuntimeNodeInfo;

import java.util.List;

/**
 * 管理端语音目录门面。
 */
public interface AdminVoiceCatalogFacade {

    PageResult<VoiceProviderInfo> findProviderPage(PageQuery pageQuery, VoiceProviderSearch search);

    List<VoiceProviderInfo> listProviderOptions(Boolean enabledOnly);

    VoiceProviderInfo findProviderById(Long id);

    Boolean createProvider(VoiceProviderUpsertRequest request);

    Boolean updateProvider(Long id, VoiceProviderUpsertRequest request);

    Boolean deleteProvider(Long id);

    Boolean updateProviderStatus(Long id, Integer status);

    PageResult<VoiceModelInfo> findModelPage(PageQuery pageQuery, VoiceModelSearch search);

    List<VoiceModelInfo> listModelOptions(Boolean enabledOnly);

    VoiceModelInfo findModelById(Long id);

    Boolean createModel(VoiceModelUpsertRequest request);

    Boolean updateModel(Long id, VoiceModelUpsertRequest request);

    Boolean deleteModel(Long id);

    Boolean updateModelStatus(Long id, Integer status);

    PageResult<VoiceProfileInfo> findProfilePage(PageQuery pageQuery, VoiceProfileSearch search);

    List<VoiceProfileInfo> listProfileOptions(Boolean enabledOnly);

    VoiceProfileInfo findProfileById(Long id);

    Boolean createProfile(VoiceProfileUpsertRequest request);

    Boolean updateProfile(Long id, VoiceProfileUpsertRequest request);

    Boolean deleteProfile(Long id);

    Boolean updateProfileStatus(Long id, Integer status);

    List<VoiceRuntimeNodeInfo> listRuntimeNodes();
}