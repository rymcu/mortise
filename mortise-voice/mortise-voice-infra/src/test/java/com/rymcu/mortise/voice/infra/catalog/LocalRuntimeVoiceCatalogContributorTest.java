package com.rymcu.mortise.voice.infra.catalog;

import com.rymcu.mortise.voice.kernel.catalog.VoiceCatalogContribution;
import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import com.rymcu.mortise.voice.kernel.model.VoiceRuntimeNodeStatus;
import com.rymcu.mortise.voice.kernel.spi.VoiceRuntimeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalRuntimeVoiceCatalogContributorTest {

    @Mock
    private VoiceRuntimeClient voiceRuntimeClient;

    @Test
    void shouldContributeLocalAsrCatalogWhenRuntimeIsHealthy() {
        when(voiceRuntimeClient.listNodes()).thenReturn(List.of(nodeStatus("healthy")));

        LocalRuntimeVoiceCatalogContributor contributor = new LocalRuntimeVoiceCatalogContributor(
                voiceRuntimeClient,
                createVoiceProperties(true, true)
        );

        List<VoiceCatalogContribution> contributions = contributor.contribute();

        assertEquals(1, contributions.size());
        VoiceCatalogContribution contribution = contributions.get(0);
        assertEquals(LocalRuntimeVoiceCatalogContributor.LOCAL_RUNTIME_PROVIDER_CODE, contribution.provider().code());
        assertEquals(1, contribution.models().size());
        assertEquals(LocalRuntimeVoiceCatalogContributor.DEFAULT_ASR_MODEL_CODE, contribution.models().get(0).code());
        assertEquals(1, contribution.profiles().size());
        assertEquals(LocalRuntimeVoiceCatalogContributor.DEFAULT_ASR_PROFILE_CODE, contribution.profiles().get(0).code());
        assertNull(contribution.profiles().get(0).ttsProviderCode());
        assertNull(contribution.profiles().get(0).ttsModelCode());
    }

    @Test
    void shouldSkipContributionWhenRuntimeIsUnhealthyAndHealthCheckIsRequired() {
        when(voiceRuntimeClient.listNodes()).thenReturn(List.of(nodeStatus("unhealthy")));

        LocalRuntimeVoiceCatalogContributor contributor = new LocalRuntimeVoiceCatalogContributor(
                voiceRuntimeClient,
                createVoiceProperties(true, true)
        );

        assertTrue(contributor.contribute().isEmpty());
    }

    @Test
    void shouldContributeWhenHealthCheckIsDisabledAndNodeIsConfigured() {
        LocalRuntimeVoiceCatalogContributor contributor = new LocalRuntimeVoiceCatalogContributor(
                voiceRuntimeClient,
                createVoiceProperties(true, false)
        );

        assertEquals(1, contributor.contribute().size());
    }

    @Test
    void shouldSkipContributionWhenRuntimeNodeIsNotConfigured() {
        LocalRuntimeVoiceCatalogContributor contributor = new LocalRuntimeVoiceCatalogContributor(
                voiceRuntimeClient,
                createVoiceProperties(false, true)
        );

        assertTrue(contributor.contribute().isEmpty());
    }

    private VoiceRuntimeNodeStatus nodeStatus(String probeStatus) {
        return new VoiceRuntimeNodeStatus(
                "node-1",
                "https://voice.atdak.com",
                "configured",
                probeStatus,
                probeStatus,
                25L,
                LocalDateTime.now(),
                List.of(LocalRuntimeVoiceCatalogContributor.DEFAULT_ASR_MODEL_CODE)
        );
    }

    private VoiceProperties createVoiceProperties(boolean runtimeConfigured, boolean requireHealthyRuntime) {
        return new VoiceProperties(
                new VoiceProperties.RuntimeConfig(
                        runtimeConfigured
                                ? List.of(new VoiceProperties.RuntimeNode("node-1", "https://voice.atdak.com", true, List.of()))
                                : List.of(),
                        1500,
                        30000
                ),
                new VoiceProperties.CatalogConfig(new VoiceProperties.CatalogBootstrapConfig(Boolean.TRUE, requireHealthyRuntime)),
                null,
                null,
                null,
                null,
                null
        );
    }
}
