package com.rymcu.mortise.voice.api.facade.impl;

import com.rymcu.mortise.voice.api.assembler.VoiceApiAssembler;
import com.rymcu.mortise.voice.api.contract.request.VoiceTtsSynthesizeRequest;
import com.rymcu.mortise.voice.api.contract.response.VoiceTtsSynthesizeResponse;
import com.rymcu.mortise.voice.api.facade.VoiceTtsFacade;
import com.rymcu.mortise.voice.application.command.VoiceSynthesizeCommand;
import com.rymcu.mortise.voice.application.service.command.VoiceTtsCommandService;
import org.springframework.stereotype.Service;

/**
 * 用户端同步语音合成门面实现。
 */
@Service
public class VoiceTtsFacadeImpl implements VoiceTtsFacade {

    private final VoiceTtsCommandService voiceTtsCommandService;
    private final VoiceApiAssembler assembler;

    public VoiceTtsFacadeImpl(
            VoiceTtsCommandService voiceTtsCommandService,
            VoiceApiAssembler assembler
    ) {
        this.voiceTtsCommandService = voiceTtsCommandService;
        this.assembler = assembler;
    }

    @Override
    public VoiceTtsSynthesizeResponse synthesize(Long userId, VoiceTtsSynthesizeRequest request) {
        return assembler.toTtsResponse(voiceTtsCommandService.synthesize(new VoiceSynthesizeCommand(
                userId,
                request.profileCode(),
                request.text(),
                request.voiceName(),
                request.sourceModule()
        )));
    }
}