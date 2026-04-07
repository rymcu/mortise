package com.rymcu.mortise.voice.application.service.query.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.application.query.VoiceModelSearchQuery;
import com.rymcu.mortise.voice.application.query.VoiceProfileSearchQuery;
import com.rymcu.mortise.voice.application.query.VoiceProviderSearchQuery;
import com.rymcu.mortise.voice.application.result.VoiceModelResult;
import com.rymcu.mortise.voice.application.result.VoiceProfileResult;
import com.rymcu.mortise.voice.application.result.VoiceProviderResult;
import com.rymcu.mortise.voice.application.service.query.VoiceCatalogQueryService;
import com.rymcu.mortise.voice.entity.VoiceModel;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.entity.VoiceProvider;
import com.rymcu.mortise.voice.model.VoiceModelSearchCriteria;
import com.rymcu.mortise.voice.model.VoiceProfileSearchCriteria;
import com.rymcu.mortise.voice.model.VoiceProviderSearchCriteria;
import com.rymcu.mortise.voice.kernel.model.VoiceCapability;
import com.rymcu.mortise.voice.kernel.model.VoiceRuntimeNodeStatus;
import com.rymcu.mortise.voice.kernel.spi.VoiceRuntimeClient;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 语音目录查询服务实现。
 */
@Service
public class VoiceCatalogQueryServiceImpl implements VoiceCatalogQueryService {

    private final VoiceProviderRepository voiceProviderRepository;
    private final VoiceModelRepository voiceModelRepository;
    private final VoiceProfileRepository voiceProfileRepository;
    private final VoiceRuntimeClient voiceRuntimeClient;

    public VoiceCatalogQueryServiceImpl(
            VoiceProviderRepository voiceProviderRepository,
            VoiceModelRepository voiceModelRepository,
            VoiceProfileRepository voiceProfileRepository,
            VoiceRuntimeClient voiceRuntimeClient
    ) {
        this.voiceProviderRepository = voiceProviderRepository;
        this.voiceModelRepository = voiceModelRepository;
        this.voiceProfileRepository = voiceProfileRepository;
        this.voiceRuntimeClient = voiceRuntimeClient;
    }

    @Override
    public List<VoiceProviderResult> listProviders(Boolean enabledOnly) {
        return voiceProviderRepository.findAll(enabledOnly).stream()
                .map(this::toProviderResult)
                .toList();
    }

    @Override
    public PageResult<VoiceProviderResult> findProviders(PageQuery pageQuery, VoiceProviderSearchQuery searchQuery) {
        return voiceProviderRepository.findProviders(pageQuery, toCriteria(searchQuery)).map(this::toProviderResult);
    }

    @Override
    public VoiceProviderResult findProviderById(Long id) {
        return voiceProviderRepository.findById(id)
                .map(this::toProviderResult)
                .orElseThrow(() -> new BusinessException("语音提供商不存在"));
    }

    @Override
    public List<VoiceModelResult> listModels(Boolean enabledOnly) {
        return voiceModelRepository.findAll(enabledOnly).stream()
                .map(this::toModelResult)
                .toList();
    }

    @Override
    public PageResult<VoiceModelResult> findModels(PageQuery pageQuery, VoiceModelSearchQuery searchQuery) {
        return voiceModelRepository.findModels(pageQuery, toCriteria(searchQuery)).map(this::toModelResult);
    }

    @Override
    public VoiceModelResult findModelById(Long id) {
        return voiceModelRepository.findById(id)
                .map(this::toModelResult)
                .orElseThrow(() -> new BusinessException("语音模型不存在"));
    }

    @Override
    public List<VoiceProfileResult> listProfiles(Boolean enabledOnly) {
        Map<Long, VoiceProvider> enabledProviders = Collections.emptyMap();
        Map<Long, VoiceModel> enabledModels = Collections.emptyMap();
        if (Boolean.TRUE.equals(enabledOnly)) {
            enabledProviders = voiceProviderRepository.findAll(true).stream()
                .collect(Collectors.toMap(VoiceProvider::getId, Function.identity()));
            enabledModels = voiceModelRepository.findAll(true).stream()
                .collect(Collectors.toMap(VoiceModel::getId, Function.identity()));
        }
        Map<Long, VoiceProvider> providerIndex = enabledProviders;
        Map<Long, VoiceModel> modelIndex = enabledModels;
        return voiceProfileRepository.findAll(enabledOnly).stream()
            .filter(profile -> !Boolean.TRUE.equals(enabledOnly) || isProfileSelectable(profile, providerIndex, modelIndex))
                .map(this::toProfileResult)
                .toList();
    }

        @Override
        public PageResult<VoiceProfileResult> findProfiles(PageQuery pageQuery, VoiceProfileSearchQuery searchQuery) {
        return voiceProfileRepository.findProfiles(pageQuery, toCriteria(searchQuery)).map(this::toProfileResult);
        }

    @Override
    public VoiceProfileResult findProfileById(Long id) {
        return voiceProfileRepository.findById(id)
                .map(this::toProfileResult)
                .orElseThrow(() -> new BusinessException("语音配置不存在"));
    }

    @Override
    public List<VoiceRuntimeNodeStatus> listRuntimeNodes() {
        return voiceRuntimeClient.listNodes();
    }

    private VoiceProviderSearchCriteria toCriteria(VoiceProviderSearchQuery searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        return new VoiceProviderSearchCriteria(normalizeQuery(searchQuery.query()), searchQuery.status());
    }

    private VoiceModelSearchCriteria toCriteria(VoiceModelSearchQuery searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        return new VoiceModelSearchCriteria(
                normalizeQuery(searchQuery.query()),
                searchQuery.status(),
                searchQuery.providerId(),
                normalizeEnumText(searchQuery.capability())
        );
    }

    private VoiceProfileSearchCriteria toCriteria(VoiceProfileSearchQuery searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        String query = normalizeQuery(searchQuery.query());
        List<Long> providerIds = StringUtils.hasText(query)
                ? voiceProviderRepository.findIdsByKeyword(query)
                : List.of();
        List<Long> modelIds = StringUtils.hasText(query)
                ? voiceModelRepository.findIdsByKeyword(query)
                : List.of();
        return new VoiceProfileSearchCriteria(query, searchQuery.status(), providerIds, modelIds);
    }

    private boolean isProfileSelectable(
            VoiceProfile profile,
            Map<Long, VoiceProvider> enabledProviders,
            Map<Long, VoiceModel> enabledModels
    ) {
        return isSelectableSlot(profile.getAsrProviderId(), profile.getAsrModelId(), VoiceCapability.ASR.name(), enabledProviders, enabledModels)
                && isSelectableSlot(profile.getVadProviderId(), profile.getVadModelId(), VoiceCapability.VAD.name(), enabledProviders, enabledModels)
                && isSelectableSlot(profile.getTtsProviderId(), profile.getTtsModelId(), VoiceCapability.TTS.name(), enabledProviders, enabledModels);
    }

    private boolean isSelectableSlot(
            Long providerId,
            Long modelId,
            String capability,
            Map<Long, VoiceProvider> enabledProviders,
            Map<Long, VoiceModel> enabledModels
    ) {
        if (providerId == null && modelId == null) {
            return true;
        }
        if (providerId == null || modelId == null) {
            return false;
        }
        VoiceProvider provider = enabledProviders.get(providerId);
        if (provider == null) {
            return false;
        }
        VoiceModel model = enabledModels.get(modelId);
        if (model == null) {
            return false;
        }
        return Objects.equals(model.getProviderId(), providerId)
                && capability.equalsIgnoreCase(model.getCapability());
    }

    private String normalizeQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.strip();
    }

    private String normalizeEnumText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.strip().toUpperCase();
    }

    private VoiceProviderResult toProviderResult(VoiceProvider provider) {
        return new VoiceProviderResult(
                provider.getId(),
                provider.getName(),
                provider.getCode(),
                provider.getProviderType(),
                provider.getStatus(),
                provider.getSortNo(),
                provider.getDefaultConfig(),
                provider.getRemark(),
                provider.getCreatedTime(),
                provider.getUpdatedTime()
        );
    }

    private VoiceModelResult toModelResult(VoiceModel model) {
        return new VoiceModelResult(
                model.getId(),
                model.getProviderId(),
                model.getName(),
                model.getCode(),
                model.getCapability(),
                model.getModelType(),
                model.getRuntimeName(),
                model.getVersion(),
                model.getLanguage(),
                model.getStatus(),
                model.getConcurrencyLimit(),
                model.getDefaultModel(),
                model.getRemark(),
                model.getCreatedTime(),
                model.getUpdatedTime()
        );
    }

    private VoiceProfileResult toProfileResult(VoiceProfile profile) {
        return new VoiceProfileResult(
                profile.getId(),
                profile.getName(),
                profile.getCode(),
                profile.getLanguage(),
                profile.getAsrProviderId(),
                profile.getAsrModelId(),
                profile.getVadProviderId(),
                profile.getVadModelId(),
                profile.getTtsProviderId(),
                profile.getTtsModelId(),
                profile.getDefaultParams(),
                profile.getStatus(),
                profile.getSortNo(),
                profile.getRemark(),
                profile.getCreatedTime(),
                profile.getUpdatedTime()
        );
    }
}