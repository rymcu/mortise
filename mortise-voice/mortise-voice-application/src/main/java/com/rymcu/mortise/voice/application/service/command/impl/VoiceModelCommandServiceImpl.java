package com.rymcu.mortise.voice.application.service.command.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.voice.application.command.VoiceModelUpsertCommand;
import com.rymcu.mortise.voice.application.service.command.VoiceModelCommandService;
import com.rymcu.mortise.voice.entity.VoiceModel;
import com.rymcu.mortise.voice.entity.VoiceProvider;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

/**
 * 语音模型命令服务实现。
 */
@Service
public class VoiceModelCommandServiceImpl implements VoiceModelCommandService {

    private final VoiceModelRepository voiceModelRepository;
    private final VoiceProviderRepository voiceProviderRepository;
    private final VoiceProfileRepository voiceProfileRepository;

    public VoiceModelCommandServiceImpl(
            VoiceModelRepository voiceModelRepository,
            VoiceProviderRepository voiceProviderRepository,
            VoiceProfileRepository voiceProfileRepository
    ) {
        this.voiceModelRepository = voiceModelRepository;
        this.voiceProviderRepository = voiceProviderRepository;
        this.voiceProfileRepository = voiceProfileRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createModel(VoiceModelUpsertCommand command) {
        VoiceProvider provider = requireProvider(command.providerId());
        Integer targetStatus = normalizeStatus(command.status());
        validateProviderStatus(provider, targetStatus);
        String code = normalizeRequiredText(command.code());
        ensureUniqueCode(code, null);
        validateDefaultModel(null, command.providerId(), normalizeEnumText(command.capability()), command.defaultModel());
        validateConcurrencyLimit(command.concurrencyLimit());
        return voiceModelRepository.save(toEntity(command, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateModel(Long id, VoiceModelUpsertCommand command) {
        requireModel(id);
        VoiceProvider provider = requireProvider(command.providerId());
        Integer targetStatus = normalizeStatus(command.status());
        validateProviderStatus(provider, targetStatus);
        String code = normalizeRequiredText(command.code());
        ensureUniqueCode(code, id);
        validateDefaultModel(id, command.providerId(), normalizeEnumText(command.capability()), command.defaultModel());
        validateConcurrencyLimit(command.concurrencyLimit());
        return voiceModelRepository.update(toEntity(command, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteModel(Long id) {
        requireModel(id);
        boolean hasProfileReference = voiceProfileRepository.existsAnyByModelId(id, null);
        if (hasProfileReference) {
            throw new BusinessException("语音模型已被配置引用，无法删除");
        }
        return voiceModelRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateModelStatus(Long id, Integer status) {
        VoiceModel model = requireModel(id);
        Integer targetStatus = normalizeStatus(status);
        if (Objects.equals(targetStatus, Status.ENABLED.getCode())) {
            validateProviderStatus(requireProvider(model.getProviderId()), targetStatus);
        }
        if (Objects.equals(targetStatus, Status.DISABLED.getCode())) {
            validateModelCanDisable(id);
        }
        return voiceModelRepository.updateStatus(id, targetStatus);
    }

    private void ensureUniqueCode(String code, Long currentId) {
        voiceModelRepository.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !Objects.equals(existing.getId(), currentId)) {
                throw new BusinessException("语音模型编码已存在");
            }
        });
    }

    private VoiceProvider requireProvider(Long providerId) {
        if (providerId == null) {
            throw new BusinessException("语音提供商不存在");
        }
        return voiceProviderRepository.findById(providerId)
                .orElseThrow(() -> new BusinessException("语音提供商不存在"));
    }

    private void validateProviderStatus(VoiceProvider provider, Integer targetStatus) {
        if (Objects.equals(targetStatus, Status.ENABLED.getCode())
                && !Objects.equals(provider.getStatus(), Status.ENABLED.getCode())) {
            throw new BusinessException("语音模型所属提供商未启用，无法启用模型");
        }
    }

    private void validateModelCanDisable(Long modelId) {
        boolean hasEnabledProfileReference = voiceProfileRepository.existsAnyByModelId(modelId, Status.ENABLED.getCode());
        if (hasEnabledProfileReference) {
            throw new BusinessException("语音模型仍被启用中的配置引用，请先禁用相关配置");
        }
    }

    private VoiceModel requireModel(Long id) {
        return voiceModelRepository.findById(id)
                .orElseThrow(() -> new BusinessException("语音模型不存在"));
    }

    private void validateDefaultModel(Long currentId, Long providerId, String capability, Boolean defaultModel) {
        if (!Boolean.TRUE.equals(defaultModel)) {
            return;
        }
        boolean duplicatedDefault = voiceModelRepository.existsDefaultModel(providerId, capability, currentId);
        if (duplicatedDefault) {
            throw new BusinessException("同一提供商下该能力已存在默认模型");
        }
    }

    private void validateConcurrencyLimit(Integer concurrencyLimit) {
        if (concurrencyLimit != null && concurrencyLimit <= 0) {
            throw new BusinessException("并发限制必须大于 0");
        }
    }

    private VoiceModel toEntity(VoiceModelUpsertCommand command, Long id) {
        VoiceModel model = new VoiceModel();
        model.setId(id);
        model.setProviderId(command.providerId());
        model.setName(normalizeRequiredText(command.name()));
        model.setCode(normalizeRequiredText(command.code()));
        model.setCapability(normalizeEnumText(command.capability()));
        model.setModelType(normalizeEnumText(command.modelType()));
        model.setRuntimeName(normalizeOptionalText(command.runtimeName()));
        model.setVersion(normalizeOptionalText(command.version()));
        model.setLanguage(normalizeOptionalText(command.language()));
        model.setConcurrencyLimit(command.concurrencyLimit());
        model.setDefaultModel(Boolean.TRUE.equals(command.defaultModel()));
        model.setStatus(normalizeStatus(command.status()));
        model.setRemark(normalizeOptionalText(command.remark()));
        return model;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return Status.ENABLED.getCode();
        }
        if (!Objects.equals(status, Status.ENABLED.getCode()) && !Objects.equals(status, Status.DISABLED.getCode())) {
            throw new BusinessException("语音模型状态值无效");
        }
        return status;
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.strip();
    }

    private String normalizeEnumText(String value) {
        return normalizeRequiredText(value).toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip();
        return normalized.isEmpty() ? null : normalized;
    }
}