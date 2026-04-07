package com.rymcu.mortise.voice.application.service.command.impl;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.voice.application.command.VoiceProfileUpsertCommand;
import com.rymcu.mortise.voice.application.service.command.VoiceProfileCommandService;
import com.rymcu.mortise.voice.application.support.VoiceCatalogValidationSupport;
import com.rymcu.mortise.voice.entity.VoiceModel;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.entity.VoiceProvider;
import com.rymcu.mortise.voice.kernel.model.VoiceCapability;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 语音配置命令服务实现。
 */
@Service
public class VoiceProfileCommandServiceImpl implements VoiceProfileCommandService {

    private final VoiceProfileRepository voiceProfileRepository;
    private final VoiceProviderRepository voiceProviderRepository;
    private final VoiceModelRepository voiceModelRepository;

    public VoiceProfileCommandServiceImpl(
            VoiceProfileRepository voiceProfileRepository,
            VoiceProviderRepository voiceProviderRepository,
            VoiceModelRepository voiceModelRepository
    ) {
        this.voiceProfileRepository = voiceProfileRepository;
        this.voiceProviderRepository = voiceProviderRepository;
        this.voiceModelRepository = voiceModelRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProfile(VoiceProfileUpsertCommand command) {
        Integer targetStatus = normalizeStatus(command.status());
        String code = normalizeRequiredText(command.code());
        ensureUniqueCode(code, null);
        validateProfileReferences(command, Objects.equals(targetStatus, Status.ENABLED.getCode()));
        return voiceProfileRepository.save(toEntity(command, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProfile(Long id, VoiceProfileUpsertCommand command) {
        requireProfile(id);
        Integer targetStatus = normalizeStatus(command.status());
        String code = normalizeRequiredText(command.code());
        ensureUniqueCode(code, id);
        validateProfileReferences(command, Objects.equals(targetStatus, Status.ENABLED.getCode()));
        return voiceProfileRepository.update(toEntity(command, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProfile(Long id) {
        requireProfile(id);
        return voiceProfileRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProfileStatus(Long id, Integer status) {
        VoiceProfile profile = requireProfile(id);
        Integer targetStatus = normalizeStatus(status);
        if (Objects.equals(targetStatus, Status.ENABLED.getCode())) {
            validateProfileReferences(profile, true);
        }
        return voiceProfileRepository.updateStatus(id, targetStatus);
    }

    private void ensureUniqueCode(String code, Long currentId) {
        voiceProfileRepository.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !Objects.equals(existing.getId(), currentId)) {
                throw new BusinessException("语音配置编码已存在");
            }
        });
    }

    private VoiceProfile requireProfile(Long id) {
        return voiceProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException("语音配置不存在"));
    }

    private void validateProfileReferences(VoiceProfileUpsertCommand command, boolean requireEnabled) {
        validateSlot("ASR", command.asrProviderId(), command.asrModelId(), VoiceCapability.ASR.name(), requireEnabled);
        validateSlot("VAD", command.vadProviderId(), command.vadModelId(), VoiceCapability.VAD.name(), requireEnabled);
        validateSlot("TTS", command.ttsProviderId(), command.ttsModelId(), VoiceCapability.TTS.name(), requireEnabled);
    }

    private void validateProfileReferences(VoiceProfile profile, boolean requireEnabled) {
        validateSlot("ASR", profile.getAsrProviderId(), profile.getAsrModelId(), VoiceCapability.ASR.name(), requireEnabled);
        validateSlot("VAD", profile.getVadProviderId(), profile.getVadModelId(), VoiceCapability.VAD.name(), requireEnabled);
        validateSlot("TTS", profile.getTtsProviderId(), profile.getTtsModelId(), VoiceCapability.TTS.name(), requireEnabled);
    }

    private void validateSlot(String slotName, Long providerId, Long modelId, String expectedCapability, boolean requireEnabled) {
        if (providerId == null && modelId == null) {
            return;
        }
        if (providerId == null || modelId == null) {
            throw new BusinessException(slotName + " 提供商与模型必须成对配置");
        }
        VoiceProvider provider = voiceProviderRepository.findById(providerId)
                .orElseThrow(() -> new BusinessException(slotName + " 提供商不存在"));
        VoiceModel model = voiceModelRepository.findById(modelId)
                .orElseThrow(() -> new BusinessException(slotName + " 模型不存在"));
        if (!Objects.equals(model.getProviderId(), providerId)) {
            throw new BusinessException(slotName + " 模型与提供商不匹配");
        }
        if (!expectedCapability.equalsIgnoreCase(model.getCapability())) {
            throw new BusinessException(slotName + " 模型能力类型不匹配");
        }
        if (requireEnabled && !Objects.equals(provider.getStatus(), Status.ENABLED.getCode())) {
            throw new BusinessException(slotName + " 提供商未启用，无法启用配置");
        }
        if (requireEnabled && !Objects.equals(model.getStatus(), Status.ENABLED.getCode())) {
            throw new BusinessException(slotName + " 模型未启用，无法启用配置");
        }
    }

    private VoiceProfile toEntity(VoiceProfileUpsertCommand command, Long id) {
        VoiceProfile profile = new VoiceProfile();
        profile.setId(id);
        profile.setName(normalizeRequiredText(command.name()));
        profile.setCode(normalizeRequiredText(command.code()));
        profile.setLanguage(normalizeOptionalText(command.language()));
        profile.setAsrProviderId(command.asrProviderId());
        profile.setAsrModelId(command.asrModelId());
        profile.setVadProviderId(command.vadProviderId());
        profile.setVadModelId(command.vadModelId());
        profile.setTtsProviderId(command.ttsProviderId());
        profile.setTtsModelId(command.ttsModelId());
        profile.setDefaultParams(VoiceCatalogValidationSupport.normalizeOptionalJsonObject(
            command.defaultParams(),
            "语音配置默认参数"
        ));
        profile.setStatus(normalizeStatus(command.status()));
        profile.setSortNo(command.sortNo() == null ? 0 : command.sortNo());
        profile.setRemark(normalizeOptionalText(command.remark()));
        return profile;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return Status.ENABLED.getCode();
        }
        if (!Objects.equals(status, Status.ENABLED.getCode()) && !Objects.equals(status, Status.DISABLED.getCode())) {
            throw new BusinessException("语音配置状态值无效");
        }
        return status;
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.strip();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip();
        return normalized.isEmpty() ? null : normalized;
    }
}