package com.rymcu.mortise.voice.application.service.bootstrap;

import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.voice.application.command.VoiceModelUpsertCommand;
import com.rymcu.mortise.voice.application.command.VoiceProfileUpsertCommand;
import com.rymcu.mortise.voice.application.command.VoiceProviderUpsertCommand;
import com.rymcu.mortise.voice.application.service.command.VoiceModelCommandService;
import com.rymcu.mortise.voice.application.service.command.VoiceProfileCommandService;
import com.rymcu.mortise.voice.application.service.command.VoiceProviderCommandService;
import com.rymcu.mortise.voice.entity.VoiceModel;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.entity.VoiceProvider;
import com.rymcu.mortise.voice.kernel.catalog.VoiceCatalogContribution;
import com.rymcu.mortise.voice.kernel.catalog.VoiceCatalogContributor;
import com.rymcu.mortise.voice.kernel.catalog.VoiceModelDescriptor;
import com.rymcu.mortise.voice.kernel.catalog.VoiceProfileDescriptor;
import com.rymcu.mortise.voice.kernel.catalog.VoiceProviderDescriptor;
import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 负责同步系统贡献的默认语音目录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceCatalogBootstrapService {

    private final VoiceProviderCommandService voiceProviderCommandService;
    private final VoiceModelCommandService voiceModelCommandService;
    private final VoiceProfileCommandService voiceProfileCommandService;
    private final VoiceProviderRepository voiceProviderRepository;
    private final VoiceModelRepository voiceModelRepository;
    private final VoiceProfileRepository voiceProfileRepository;
    private final List<VoiceCatalogContributor> contributors;
    private final VoiceProperties voiceProperties;

    private final AtomicBoolean bootstrapped = new AtomicBoolean(false);

    @Transactional(rollbackFor = Exception.class)
    public void bootstrapIfNecessary() {
        if (!bootstrapped.compareAndSet(false, true)) {
            log.debug("跳过语音目录启动同步：当前实例已执行过同步");
            return;
        }
        if (!Boolean.TRUE.equals(voiceProperties.catalog().bootstrap().enabled())) {
            log.info("跳过语音目录启动同步：catalog bootstrap 已禁用");
            return;
        }
        if (contributors.isEmpty()) {
            log.info("跳过语音目录启动同步：未注册目录贡献者");
            return;
        }

        int synchronizedContributions = 0;
        for (VoiceCatalogContributor contributor : contributors) {
            List<VoiceCatalogContribution> contributions = contributor.contribute();
            if (contributions.isEmpty()) {
                log.info("目录贡献者未产出默认目录: {}", contributor.getClass().getSimpleName());
                continue;
            }
            for (VoiceCatalogContribution contribution : contributions) {
                syncContribution(contribution);
                synchronizedContributions++;
            }
        }

        if (synchronizedContributions == 0) {
            log.info("跳过语音目录启动同步：没有可同步的系统默认目录");
            return;
        }
        log.info("语音目录启动同步完成：contributions={}", synchronizedContributions);
    }

    private void syncContribution(VoiceCatalogContribution contribution) {
        VoiceProvider provider = ensureProvider(contribution.provider());
        for (VoiceModelDescriptor modelDescriptor : contribution.models()) {
            ensureModel(modelDescriptor, provider.getId());
        }
        for (VoiceProfileDescriptor profileDescriptor : contribution.profiles()) {
            ensureProfile(profileDescriptor);
        }
    }

    private VoiceProvider ensureProvider(VoiceProviderDescriptor descriptor) {
        VoiceProvider existing = voiceProviderRepository.findByCode(descriptor.code()).orElse(null);
        VoiceProviderUpsertCommand command = buildProviderCommand(descriptor, existing);
        if (existing == null) {
            requireSuccess(voiceProviderCommandService.createProvider(command), "创建系统语音 Provider 失败: " + descriptor.code());
            log.info("已补齐系统语音 Provider: {}", descriptor.code());
            return loadProviderByCode(descriptor.code());
        }
        if (!providerMatches(existing, command)) {
            requireSuccess(voiceProviderCommandService.updateProvider(existing.getId(), command), "更新系统语音 Provider 失败: " + descriptor.code());
            log.info("已修正系统语音 Provider: {}", descriptor.code());
            return loadProviderByCode(descriptor.code());
        }
        return existing;
    }

    private VoiceModel ensureModel(VoiceModelDescriptor descriptor, Long providerId) {
        VoiceModel existing = voiceModelRepository.findByCode(descriptor.code()).orElse(null);
        boolean desiredDefaultModel = resolveDesiredDefaultModel(descriptor, providerId, existing != null ? existing.getId() : null);
        VoiceModelUpsertCommand command = buildModelCommand(descriptor, providerId, existing, desiredDefaultModel);
        if (existing == null) {
            requireSuccess(voiceModelCommandService.createModel(command), "创建系统语音模型失败: " + descriptor.code());
            log.info("已补齐系统语音模型: {}", descriptor.code());
            return loadModelByCode(descriptor.code());
        }
        if (!modelMatches(existing, command)) {
            requireSuccess(voiceModelCommandService.updateModel(existing.getId(), command), "更新系统语音模型失败: " + descriptor.code());
            log.info("已修正系统语音模型: {}", descriptor.code());
            return loadModelByCode(descriptor.code());
        }
        return existing;
    }

    private VoiceProfile ensureProfile(VoiceProfileDescriptor descriptor) {
        VoiceProfile existing = voiceProfileRepository.findByCode(descriptor.code()).orElse(null);
        ResolvedSlot asrSlot = resolveSlot("ASR", descriptor.asrProviderCode(), descriptor.asrModelCode());
        ResolvedSlot vadSlot = resolveSlot("VAD", descriptor.vadProviderCode(), descriptor.vadModelCode());
        ResolvedSlot ttsSlot = resolveSlot("TTS", descriptor.ttsProviderCode(), descriptor.ttsModelCode());
        VoiceProfileUpsertCommand command = buildProfileCommand(descriptor, asrSlot, vadSlot, ttsSlot, existing);
        if (existing == null) {
            requireSuccess(voiceProfileCommandService.createProfile(command), "创建系统语音配置失败: " + descriptor.code());
            log.info("已补齐系统语音配置: {}", descriptor.code());
            return loadProfileByCode(descriptor.code());
        }
        if (!profileMatches(existing, command)) {
            requireSuccess(voiceProfileCommandService.updateProfile(existing.getId(), command), "更新系统语音配置失败: " + descriptor.code());
            log.info("已修正系统语音配置: {}", descriptor.code());
            return loadProfileByCode(descriptor.code());
        }
        return existing;
    }

    private boolean resolveDesiredDefaultModel(VoiceModelDescriptor descriptor, Long providerId, Long currentModelId) {
        if (!descriptor.defaultModel()) {
            return false;
        }
        boolean duplicatedDefault = voiceModelRepository.existsDefaultModel(providerId, descriptor.capability().name(), currentModelId);
        if (duplicatedDefault) {
            log.warn("系统语音模型 {} 遇到现有默认模型冲突，本次以 defaultModel=false 同步", descriptor.code());
            return false;
        }
        return true;
    }

    private ResolvedSlot resolveSlot(String slotName, String providerCode, String modelCode) {
        if (!StringUtils.hasText(providerCode) && !StringUtils.hasText(modelCode)) {
            return new ResolvedSlot(null, null);
        }
        if (!StringUtils.hasText(providerCode) || !StringUtils.hasText(modelCode)) {
            throw new ServiceException(slotName + " 系统语音配置缺少 providerCode/modelCode 配对");
        }
        Long providerId = loadProviderByCode(providerCode.strip()).getId();
        Long modelId = loadModelByCode(modelCode.strip()).getId();
        return new ResolvedSlot(providerId, modelId);
    }

    private VoiceProviderUpsertCommand buildProviderCommand(VoiceProviderDescriptor descriptor, VoiceProvider existing) {
        return new VoiceProviderUpsertCommand(
                descriptor.name(),
                descriptor.code(),
                descriptor.providerType().name(),
                descriptor.status(),
                descriptor.sortNo(),
                descriptor.defaultConfig() != null ? descriptor.defaultConfig() : existing != null ? existing.getDefaultConfig() : null,
                descriptor.remark() != null ? descriptor.remark() : existing != null ? existing.getRemark() : null
        );
    }

    private VoiceModelUpsertCommand buildModelCommand(
            VoiceModelDescriptor descriptor,
            Long providerId,
            VoiceModel existing,
            boolean desiredDefaultModel
    ) {
        return new VoiceModelUpsertCommand(
                providerId,
                descriptor.name(),
                descriptor.code(),
                descriptor.capability().name(),
                descriptor.modelType().name(),
                descriptor.runtimeName(),
                descriptor.version() != null ? descriptor.version() : existing != null ? existing.getVersion() : null,
                descriptor.language() != null ? descriptor.language() : existing != null ? existing.getLanguage() : null,
                descriptor.concurrencyLimit() != null ? descriptor.concurrencyLimit() : existing != null ? existing.getConcurrencyLimit() : null,
                desiredDefaultModel,
                descriptor.status(),
                descriptor.remark() != null ? descriptor.remark() : existing != null ? existing.getRemark() : null
        );
    }

    private VoiceProfileUpsertCommand buildProfileCommand(
            VoiceProfileDescriptor descriptor,
            ResolvedSlot asrSlot,
            ResolvedSlot vadSlot,
            ResolvedSlot ttsSlot,
            VoiceProfile existing
    ) {
        return new VoiceProfileUpsertCommand(
                descriptor.name(),
                descriptor.code(),
                descriptor.language() != null ? descriptor.language() : existing != null ? existing.getLanguage() : null,
                asrSlot.providerId(),
                asrSlot.modelId(),
                vadSlot.providerId(),
                vadSlot.modelId(),
                ttsSlot.providerId(),
                ttsSlot.modelId(),
                descriptor.defaultParams() != null ? descriptor.defaultParams() : existing != null ? existing.getDefaultParams() : null,
                descriptor.status(),
                descriptor.sortNo(),
                descriptor.remark() != null ? descriptor.remark() : existing != null ? existing.getRemark() : null
        );
    }

    private boolean providerMatches(VoiceProvider provider, VoiceProviderUpsertCommand command) {
        return Objects.equals(provider.getName(), command.name())
                && Objects.equals(provider.getCode(), command.code())
                && Objects.equals(provider.getProviderType(), command.providerType())
                && Objects.equals(provider.getStatus(), command.status())
                && Objects.equals(provider.getSortNo(), command.sortNo())
                && Objects.equals(provider.getDefaultConfig(), command.defaultConfig())
                && Objects.equals(provider.getRemark(), command.remark());
    }

    private boolean modelMatches(VoiceModel model, VoiceModelUpsertCommand command) {
        return Objects.equals(model.getProviderId(), command.providerId())
                && Objects.equals(model.getName(), command.name())
                && Objects.equals(model.getCode(), command.code())
                && Objects.equals(model.getCapability(), command.capability())
                && Objects.equals(model.getModelType(), command.modelType())
                && Objects.equals(model.getRuntimeName(), command.runtimeName())
                && Objects.equals(model.getVersion(), command.version())
                && Objects.equals(model.getLanguage(), command.language())
                && Objects.equals(model.getConcurrencyLimit(), command.concurrencyLimit())
                && Objects.equals(model.getDefaultModel(), command.defaultModel())
                && Objects.equals(model.getStatus(), command.status())
                && Objects.equals(model.getRemark(), command.remark());
    }

    private boolean profileMatches(VoiceProfile profile, VoiceProfileUpsertCommand command) {
        return Objects.equals(profile.getName(), command.name())
                && Objects.equals(profile.getCode(), command.code())
                && Objects.equals(profile.getLanguage(), command.language())
                && Objects.equals(profile.getAsrProviderId(), command.asrProviderId())
                && Objects.equals(profile.getAsrModelId(), command.asrModelId())
                && Objects.equals(profile.getVadProviderId(), command.vadProviderId())
                && Objects.equals(profile.getVadModelId(), command.vadModelId())
                && Objects.equals(profile.getTtsProviderId(), command.ttsProviderId())
                && Objects.equals(profile.getTtsModelId(), command.ttsModelId())
                && Objects.equals(profile.getDefaultParams(), command.defaultParams())
                && Objects.equals(profile.getStatus(), command.status())
                && Objects.equals(profile.getSortNo(), command.sortNo())
                && Objects.equals(profile.getRemark(), command.remark());
    }

    private VoiceProvider loadProviderByCode(String code) {
        return voiceProviderRepository.findByCode(code)
                .orElseThrow(() -> new ServiceException("系统语音 Provider 不存在: " + code));
    }

    private VoiceModel loadModelByCode(String code) {
        return voiceModelRepository.findByCode(code)
                .orElseThrow(() -> new ServiceException("系统语音模型不存在: " + code));
    }

    private VoiceProfile loadProfileByCode(String code) {
        return voiceProfileRepository.findByCode(code)
                .orElseThrow(() -> new ServiceException("系统语音配置不存在: " + code));
    }

    private void requireSuccess(boolean success, String message) {
        if (!success) {
            throw new ServiceException(message);
        }
    }

    private record ResolvedSlot(Long providerId, Long modelId) {
    }
}
