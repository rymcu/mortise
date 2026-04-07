package com.rymcu.mortise.voice.admin.facade.impl;

import com.rymcu.mortise.voice.admin.assembler.AdminVoiceCatalogAssembler;
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
import com.rymcu.mortise.voice.admin.facade.AdminVoiceCatalogFacade;
import com.rymcu.mortise.voice.application.service.command.VoiceModelCommandService;
import com.rymcu.mortise.voice.application.service.command.VoiceProfileCommandService;
import com.rymcu.mortise.voice.application.service.command.VoiceProviderCommandService;
import com.rymcu.mortise.voice.application.service.query.VoiceCatalogQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端语音目录门面实现。
 */
@Service
public class AdminVoiceCatalogFacadeImpl implements AdminVoiceCatalogFacade {

    private final VoiceCatalogQueryService voiceCatalogQueryService;
    private final VoiceProviderCommandService voiceProviderCommandService;
    private final VoiceModelCommandService voiceModelCommandService;
    private final VoiceProfileCommandService voiceProfileCommandService;
    private final AdminVoiceCatalogAssembler assembler;

    public AdminVoiceCatalogFacadeImpl(
            VoiceCatalogQueryService voiceCatalogQueryService,
            VoiceProviderCommandService voiceProviderCommandService,
            VoiceModelCommandService voiceModelCommandService,
            VoiceProfileCommandService voiceProfileCommandService,
            AdminVoiceCatalogAssembler assembler
    ) {
        this.voiceCatalogQueryService = voiceCatalogQueryService;
        this.voiceProviderCommandService = voiceProviderCommandService;
        this.voiceModelCommandService = voiceModelCommandService;
        this.voiceProfileCommandService = voiceProfileCommandService;
        this.assembler = assembler;
    }

    @Override
    public PageResult<VoiceProviderInfo> findProviderPage(PageQuery pageQuery, VoiceProviderSearch search) {
        return voiceCatalogQueryService.findProviders(pageQuery, assembler.toSearchQuery(search)).map(assembler::toInfo);
    }

    @Override
    public List<VoiceProviderInfo> listProviderOptions(Boolean enabledOnly) {
        return voiceCatalogQueryService.listProviders(enabledOnly).stream()
                .map(assembler::toInfo)
                .toList();
    }

    @Override
    public VoiceProviderInfo findProviderById(Long id) {
        return assembler.toInfo(voiceCatalogQueryService.findProviderById(id));
    }

    @Override
    public Boolean createProvider(VoiceProviderUpsertRequest request) {
        return voiceProviderCommandService.createProvider(assembler.toCommand(request));
    }

    @Override
    public Boolean updateProvider(Long id, VoiceProviderUpsertRequest request) {
        return voiceProviderCommandService.updateProvider(id, assembler.toCommand(request));
    }

    @Override
    public Boolean deleteProvider(Long id) {
        return voiceProviderCommandService.deleteProvider(id);
    }

    @Override
    public Boolean updateProviderStatus(Long id, Integer status) {
        return voiceProviderCommandService.updateProviderStatus(id, status);
    }

    @Override
    public PageResult<VoiceModelInfo> findModelPage(PageQuery pageQuery, VoiceModelSearch search) {
        return voiceCatalogQueryService.findModels(pageQuery, assembler.toSearchQuery(search)).map(assembler::toInfo);
    }

    @Override
    public List<VoiceModelInfo> listModelOptions(Boolean enabledOnly) {
        return voiceCatalogQueryService.listModels(enabledOnly).stream()
                .map(assembler::toInfo)
                .toList();
    }

    @Override
    public VoiceModelInfo findModelById(Long id) {
        return assembler.toInfo(voiceCatalogQueryService.findModelById(id));
    }

    @Override
    public Boolean createModel(VoiceModelUpsertRequest request) {
        return voiceModelCommandService.createModel(assembler.toCommand(request));
    }

    @Override
    public Boolean updateModel(Long id, VoiceModelUpsertRequest request) {
        return voiceModelCommandService.updateModel(id, assembler.toCommand(request));
    }

    @Override
    public Boolean deleteModel(Long id) {
        return voiceModelCommandService.deleteModel(id);
    }

    @Override
    public Boolean updateModelStatus(Long id, Integer status) {
        return voiceModelCommandService.updateModelStatus(id, status);
    }

    @Override
    public PageResult<VoiceProfileInfo> findProfilePage(PageQuery pageQuery, VoiceProfileSearch search) {
        return voiceCatalogQueryService.findProfiles(pageQuery, assembler.toSearchQuery(search)).map(assembler::toInfo);
    }

    @Override
    public List<VoiceProfileInfo> listProfileOptions(Boolean enabledOnly) {
        return voiceCatalogQueryService.listProfiles(enabledOnly).stream()
                .map(assembler::toInfo)
                .toList();
    }

    @Override
    public VoiceProfileInfo findProfileById(Long id) {
        return assembler.toInfo(voiceCatalogQueryService.findProfileById(id));
    }

    @Override
    public Boolean createProfile(VoiceProfileUpsertRequest request) {
        return voiceProfileCommandService.createProfile(assembler.toCommand(request));
    }

    @Override
    public Boolean updateProfile(Long id, VoiceProfileUpsertRequest request) {
        return voiceProfileCommandService.updateProfile(id, assembler.toCommand(request));
    }

    @Override
    public Boolean deleteProfile(Long id) {
        return voiceProfileCommandService.deleteProfile(id);
    }

    @Override
    public Boolean updateProfileStatus(Long id, Integer status) {
        return voiceProfileCommandService.updateProfileStatus(id, status);
    }

    @Override
    public List<VoiceRuntimeNodeInfo> listRuntimeNodes() {
        return voiceCatalogQueryService.listRuntimeNodes().stream()
                .map(assembler::toInfo)
                .toList();
    }
}