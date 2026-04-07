package com.rymcu.mortise.voice.application.service.command.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.voice.application.command.VoiceProviderUpsertCommand;
import com.rymcu.mortise.voice.application.service.command.VoiceProviderCommandService;
import com.rymcu.mortise.voice.application.support.VoiceCatalogValidationSupport;
import com.rymcu.mortise.voice.entity.VoiceProvider;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 语音提供商命令服务实现。
 */
@Service
public class VoiceProviderCommandServiceImpl implements VoiceProviderCommandService {

    private final VoiceProviderRepository voiceProviderRepository;
    private final VoiceModelRepository voiceModelRepository;
    private final VoiceProfileRepository voiceProfileRepository;

    public VoiceProviderCommandServiceImpl(
            VoiceProviderRepository voiceProviderRepository,
            VoiceModelRepository voiceModelRepository,
            VoiceProfileRepository voiceProfileRepository
    ) {
        this.voiceProviderRepository = voiceProviderRepository;
        this.voiceModelRepository = voiceModelRepository;
        this.voiceProfileRepository = voiceProfileRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProvider(VoiceProviderUpsertCommand command) {
        String code = normalizeRequiredText(command.code());
        ensureUniqueCode(code, null);
        return voiceProviderRepository.save(toEntity(command, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProvider(Long id, VoiceProviderUpsertCommand command) {
        requireProvider(id);
        String code = normalizeRequiredText(command.code());
        ensureUniqueCode(code, id);
        return voiceProviderRepository.update(toEntity(command, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProvider(Long id) {
        requireProvider(id);
        boolean hasModelReference = voiceModelRepository.existsAnyByProviderId(id, null);
        if (hasModelReference) {
            throw new BusinessException("语音提供商已关联模型，无法删除");
        }
        boolean hasProfileReference = voiceProfileRepository.existsAnyByProviderId(id, null);
        if (hasProfileReference) {
            throw new BusinessException("语音提供商已被配置引用，无法删除");
        }
        return voiceProviderRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProviderStatus(Long id, Integer status) {
        requireProvider(id);
        Integer targetStatus = normalizeStatus(status);
        if (Objects.equals(targetStatus, Status.DISABLED.getCode())) {
            validateProviderCanDisable(id);
        }
        return voiceProviderRepository.updateStatus(id, targetStatus);
    }

    private void validateProviderCanDisable(Long providerId) {
        boolean hasEnabledModelReference = voiceModelRepository.existsAnyByProviderId(providerId, Status.ENABLED.getCode());
        if (hasEnabledModelReference) {
            throw new BusinessException("语音提供商下仍存在启用中的模型，请先禁用相关模型");
        }
        boolean hasEnabledProfileReference = voiceProfileRepository.existsAnyByProviderId(providerId, Status.ENABLED.getCode());
        if (hasEnabledProfileReference) {
            throw new BusinessException("语音提供商仍被启用中的配置引用，请先禁用相关配置");
        }
    }

    private void ensureUniqueCode(String code, Long currentId) {
        voiceProviderRepository.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !Objects.equals(existing.getId(), currentId)) {
                throw new BusinessException("语音提供商编码已存在");
            }
        });
    }

    private VoiceProvider requireProvider(Long id) {
        return voiceProviderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("语音提供商不存在"));
    }

    private VoiceProvider toEntity(VoiceProviderUpsertCommand command, Long id) {
        VoiceProvider provider = new VoiceProvider();
        provider.setId(id);
        provider.setName(normalizeRequiredText(command.name()));
        provider.setCode(normalizeRequiredText(command.code()));
        provider.setProviderType(normalizeEnumText(command.providerType()));
        provider.setStatus(normalizeStatus(command.status()));
        provider.setSortNo(command.sortNo() == null ? 0 : command.sortNo());
        provider.setDefaultConfig(VoiceCatalogValidationSupport.normalizeOptionalJsonObject(
                command.defaultConfig(),
                "语音提供商默认配置"
        ));
        provider.setRemark(normalizeOptionalText(command.remark()));
        return provider;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return Status.ENABLED.getCode();
        }
        if (!Objects.equals(status, Status.ENABLED.getCode()) && !Objects.equals(status, Status.DISABLED.getCode())) {
            throw new BusinessException("语音提供商状态值无效");
        }
        return status;
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.strip();
    }

    private String normalizeEnumText(String value) {
        return normalizeRequiredText(value).toUpperCase();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip();
        return normalized.isEmpty() ? null : normalized;
    }
}