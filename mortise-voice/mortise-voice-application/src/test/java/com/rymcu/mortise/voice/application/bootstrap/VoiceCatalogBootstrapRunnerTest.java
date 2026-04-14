package com.rymcu.mortise.voice.application.bootstrap;

import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.voice.application.command.VoiceModelUpsertCommand;
import com.rymcu.mortise.voice.application.command.VoiceProfileUpsertCommand;
import com.rymcu.mortise.voice.application.command.VoiceProviderUpsertCommand;
import com.rymcu.mortise.voice.application.service.bootstrap.VoiceCatalogBootstrapService;
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
import com.rymcu.mortise.voice.kernel.model.VoiceCapability;
import com.rymcu.mortise.voice.kernel.model.VoiceModelType;
import com.rymcu.mortise.voice.kernel.model.VoiceProviderType;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceCatalogBootstrapRunnerTest {

    private static final String LOCAL_RUNTIME_PROVIDER_CODE = "local-runtime";
    private static final String LOCAL_RUNTIME_PROVIDER_NAME = "Local Runtime";
    private static final String DEFAULT_ASR_MODEL_CODE = "sense-voice-zh-en-ja-ko-yue-int8";
    private static final String DEFAULT_ASR_MODEL_NAME = "SenseVoice Zh/En/Ja/Ko/Yue Int8";
    private static final String DEFAULT_ASR_PROFILE_CODE = "sense-voice-default";
    private static final String DEFAULT_ASR_PROFILE_NAME = "SenseVoice Default";

    @Mock
    private VoiceProviderCommandService voiceProviderCommandService;
    @Mock
    private VoiceModelCommandService voiceModelCommandService;
    @Mock
    private VoiceProfileCommandService voiceProfileCommandService;
    @Mock
    private VoiceProviderRepository voiceProviderRepository;
    @Mock
    private VoiceModelRepository voiceModelRepository;
    @Mock
    private VoiceProfileRepository voiceProfileRepository;
    @Mock
    private VoiceCatalogContributor contributor;

    private VoiceCatalogBootstrapRunner runner;

    @BeforeEach
    void setUp() {
        runner = new VoiceCatalogBootstrapRunner(new VoiceCatalogBootstrapService(
                voiceProviderCommandService,
                voiceModelCommandService,
                voiceProfileCommandService,
                voiceProviderRepository,
                voiceModelRepository,
                voiceProfileRepository,
                List.of(contributor),
                createVoiceProperties(true)
        ));
    }

    @Test
    void shouldCreateProviderModelAndProfileWhenContributionIsAvailable() throws Exception {
        VoiceProvider provider = createProvider(1L, LOCAL_RUNTIME_PROVIDER_NAME, Status.ENABLED.getCode());
        VoiceModel model = createModel(2L, 1L, true, Status.ENABLED.getCode());
        VoiceProfile profile = createProfile(3L, 1L, 2L, Status.ENABLED.getCode());

        when(contributor.contribute()).thenReturn(List.of(createContribution()));
        when(voiceProviderRepository.findByCode(LOCAL_RUNTIME_PROVIDER_CODE))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(provider));
        when(voiceModelRepository.findByCode(DEFAULT_ASR_MODEL_CODE))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(model));
        when(voiceModelRepository.existsDefaultModel(1L, VoiceCapability.ASR.name(), null)).thenReturn(false);
        when(voiceProfileRepository.findByCode(DEFAULT_ASR_PROFILE_CODE))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(profile));
        when(voiceProviderCommandService.createProvider(any(VoiceProviderUpsertCommand.class))).thenReturn(true);
        when(voiceModelCommandService.createModel(any(VoiceModelUpsertCommand.class))).thenReturn(true);
        when(voiceProfileCommandService.createProfile(any(VoiceProfileUpsertCommand.class))).thenReturn(true);

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<VoiceProviderUpsertCommand> providerCaptor = ArgumentCaptor.forClass(VoiceProviderUpsertCommand.class);
        verify(voiceProviderCommandService).createProvider(providerCaptor.capture());
        assertEquals(LOCAL_RUNTIME_PROVIDER_NAME, providerCaptor.getValue().name());
        assertEquals(VoiceProviderType.LOCAL_RUNTIME.name(), providerCaptor.getValue().providerType());

        ArgumentCaptor<VoiceModelUpsertCommand> modelCaptor = ArgumentCaptor.forClass(VoiceModelUpsertCommand.class);
        verify(voiceModelCommandService).createModel(modelCaptor.capture());
        assertEquals(1L, modelCaptor.getValue().providerId());
        assertEquals(VoiceCapability.ASR.name(), modelCaptor.getValue().capability());
        assertEquals(VoiceModelType.SENSEVOICE.name(), modelCaptor.getValue().modelType());
        assertEquals(DEFAULT_ASR_MODEL_CODE, modelCaptor.getValue().runtimeName());
        assertEquals(Boolean.TRUE, modelCaptor.getValue().defaultModel());

        ArgumentCaptor<VoiceProfileUpsertCommand> profileCaptor = ArgumentCaptor.forClass(VoiceProfileUpsertCommand.class);
        verify(voiceProfileCommandService).createProfile(profileCaptor.capture());
        assertEquals(1L, profileCaptor.getValue().asrProviderId());
        assertEquals(2L, profileCaptor.getValue().asrModelId());
        assertNull(profileCaptor.getValue().ttsProviderId());
        assertNull(profileCaptor.getValue().ttsModelId());
        assertNull(profileCaptor.getValue().vadProviderId());
        assertNull(profileCaptor.getValue().vadModelId());
    }

    @Test
    void shouldRepairReservedRecordsWithoutTouchingUserDefaultModel() throws Exception {
        VoiceProvider existingProvider = createProvider(11L, "Runtime Provider", Status.DISABLED.getCode());
        VoiceProvider updatedProvider = createProvider(11L, LOCAL_RUNTIME_PROVIDER_NAME, Status.ENABLED.getCode());
        VoiceModel existingModel = createModel(13L, 99L, true, Status.DISABLED.getCode());
        VoiceModel updatedModel = createModel(13L, 11L, false, Status.ENABLED.getCode());
        VoiceProfile existingProfile = createProfile(14L, 99L, 98L, Status.DISABLED.getCode());
        existingProfile.setTtsProviderId(88L);
        existingProfile.setTtsModelId(87L);
        VoiceProfile updatedProfile = createProfile(14L, 11L, 13L, Status.ENABLED.getCode());

        when(contributor.contribute()).thenReturn(List.of(createContribution()));
        when(voiceProviderRepository.findByCode(LOCAL_RUNTIME_PROVIDER_CODE))
                .thenReturn(Optional.of(existingProvider))
                .thenReturn(Optional.of(updatedProvider));
        when(voiceProviderCommandService.updateProvider(eq(11L), any(VoiceProviderUpsertCommand.class))).thenReturn(true);

        when(voiceModelRepository.findByCode(DEFAULT_ASR_MODEL_CODE))
                .thenReturn(Optional.of(existingModel))
                .thenReturn(Optional.of(updatedModel));
        when(voiceModelRepository.existsDefaultModel(11L, VoiceCapability.ASR.name(), 13L)).thenReturn(true);
        when(voiceModelCommandService.updateModel(eq(13L), any(VoiceModelUpsertCommand.class))).thenReturn(true);

        when(voiceProfileRepository.findByCode(DEFAULT_ASR_PROFILE_CODE))
                .thenReturn(Optional.of(existingProfile))
                .thenReturn(Optional.of(updatedProfile));
        when(voiceProfileCommandService.updateProfile(eq(14L), any(VoiceProfileUpsertCommand.class))).thenReturn(true);

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<VoiceModelUpsertCommand> targetModelCaptor = ArgumentCaptor.forClass(VoiceModelUpsertCommand.class);
        verify(voiceModelCommandService).updateModel(eq(13L), targetModelCaptor.capture());
        assertEquals(11L, targetModelCaptor.getValue().providerId());
        assertFalse(targetModelCaptor.getValue().defaultModel());
        assertEquals(Status.ENABLED.getCode(), targetModelCaptor.getValue().status());
        verify(voiceModelCommandService, never()).updateModel(eq(12L), any(VoiceModelUpsertCommand.class));

        ArgumentCaptor<VoiceProfileUpsertCommand> profileCaptor = ArgumentCaptor.forClass(VoiceProfileUpsertCommand.class);
        verify(voiceProfileCommandService).updateProfile(eq(14L), profileCaptor.capture());
        assertEquals(11L, profileCaptor.getValue().asrProviderId());
        assertEquals(13L, profileCaptor.getValue().asrModelId());
        assertNull(profileCaptor.getValue().ttsProviderId());
        assertNull(profileCaptor.getValue().ttsModelId());
        assertEquals(Status.ENABLED.getCode(), profileCaptor.getValue().status());
    }

    @Test
    void shouldSkipWhenContributorReturnsNoContribution() throws Exception {
        when(contributor.contribute()).thenReturn(List.of());

        runner.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(voiceProviderCommandService, voiceModelCommandService, voiceProfileCommandService);
    }

    @Test
    void shouldSkipWhenBootstrapIsDisabled() throws Exception {
        VoiceCatalogBootstrapRunner disabledRunner = new VoiceCatalogBootstrapRunner(new VoiceCatalogBootstrapService(
                voiceProviderCommandService,
                voiceModelCommandService,
                voiceProfileCommandService,
                voiceProviderRepository,
                voiceModelRepository,
                voiceProfileRepository,
                List.of(contributor),
                createVoiceProperties(false)
        ));

        disabledRunner.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(contributor, voiceProviderCommandService, voiceModelCommandService, voiceProfileCommandService);
    }

    private VoiceCatalogContribution createContribution() {
        return new VoiceCatalogContribution(
                new VoiceProviderDescriptor(
                        LOCAL_RUNTIME_PROVIDER_CODE,
                        LOCAL_RUNTIME_PROVIDER_NAME,
                        VoiceProviderType.LOCAL_RUNTIME,
                        Status.ENABLED.getCode(),
                        0,
                        null,
                        null
                ),
                List.of(new VoiceModelDescriptor(
                        LOCAL_RUNTIME_PROVIDER_CODE,
                        DEFAULT_ASR_MODEL_CODE,
                        DEFAULT_ASR_MODEL_NAME,
                        VoiceCapability.ASR,
                        VoiceModelType.SENSEVOICE,
                        DEFAULT_ASR_MODEL_CODE,
                        null,
                        null,
                        null,
                        true,
                        Status.ENABLED.getCode(),
                        null
                )),
                List.of(new VoiceProfileDescriptor(
                        DEFAULT_ASR_PROFILE_CODE,
                        DEFAULT_ASR_PROFILE_NAME,
                        null,
                        LOCAL_RUNTIME_PROVIDER_CODE,
                        DEFAULT_ASR_MODEL_CODE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Status.ENABLED.getCode(),
                        0,
                        null
                ))
        );
    }

    private VoiceProperties createVoiceProperties(boolean bootstrapEnabled) {
        return new VoiceProperties(
                null,
                new VoiceProperties.CatalogConfig(new VoiceProperties.CatalogBootstrapConfig(bootstrapEnabled, Boolean.TRUE)),
                null,
                null,
                null,
                null,
                null
        );
    }

    private VoiceProvider createProvider(Long id, String name, Integer status) {
        VoiceProvider provider = new VoiceProvider();
        provider.setId(id);
        provider.setCode(LOCAL_RUNTIME_PROVIDER_CODE);
        provider.setName(name);
        provider.setProviderType(VoiceProviderType.LOCAL_RUNTIME.name());
        provider.setStatus(status);
        provider.setSortNo(0);
        return provider;
    }

    private VoiceModel createModel(Long id, Long providerId, boolean defaultModel, Integer status) {
        VoiceModel model = new VoiceModel();
        model.setId(id);
        model.setProviderId(providerId);
        model.setCode(DEFAULT_ASR_MODEL_CODE);
        model.setName(DEFAULT_ASR_MODEL_NAME);
        model.setCapability(VoiceCapability.ASR.name());
        model.setModelType(VoiceModelType.SENSEVOICE.name());
        model.setRuntimeName(DEFAULT_ASR_MODEL_CODE);
        model.setDefaultModel(defaultModel);
        model.setStatus(status);
        return model;
    }

    private VoiceProfile createProfile(Long id, Long providerId, Long modelId, Integer status) {
        VoiceProfile profile = new VoiceProfile();
        profile.setId(id);
        profile.setCode(DEFAULT_ASR_PROFILE_CODE);
        profile.setName(DEFAULT_ASR_PROFILE_NAME);
        profile.setAsrProviderId(providerId);
        profile.setAsrModelId(modelId);
        profile.setStatus(status);
        profile.setSortNo(0);
        return profile;
    }
}
