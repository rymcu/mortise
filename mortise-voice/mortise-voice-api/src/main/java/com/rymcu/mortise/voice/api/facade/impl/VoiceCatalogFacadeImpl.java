package com.rymcu.mortise.voice.api.facade.impl;

import com.rymcu.mortise.voice.api.assembler.VoiceApiAssembler;
import com.rymcu.mortise.voice.api.contract.response.VoiceProfileOption;
import com.rymcu.mortise.voice.api.facade.VoiceCatalogFacade;
import com.rymcu.mortise.voice.application.service.query.VoiceCatalogQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户端语音目录门面实现。
 */
@Service
public class VoiceCatalogFacadeImpl implements VoiceCatalogFacade {

    private final VoiceCatalogQueryService voiceCatalogQueryService;
    private final VoiceApiAssembler assembler;

    public VoiceCatalogFacadeImpl(
            VoiceCatalogQueryService voiceCatalogQueryService,
            VoiceApiAssembler assembler
    ) {
        this.voiceCatalogQueryService = voiceCatalogQueryService;
        this.assembler = assembler;
    }

    @Override
    public List<VoiceProfileOption> listProfiles() {
        return voiceCatalogQueryService.listProfiles(true).stream()
                .map(assembler::toProfileOption)
                .toList();
    }
}