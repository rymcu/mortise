package com.rymcu.mortise.voice.api.facade.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.voice.api.assembler.VoiceApiAssembler;
import com.rymcu.mortise.voice.api.contract.response.VoiceAsrRecognizeResponse;
import com.rymcu.mortise.voice.api.facade.VoiceAsrFacade;
import com.rymcu.mortise.voice.application.command.VoiceRecognizeOnceCommand;
import com.rymcu.mortise.voice.application.service.command.VoiceAsrCommandService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户端短音频识别门面实现。
 */
@Service
public class VoiceAsrFacadeImpl implements VoiceAsrFacade {

    private final VoiceAsrCommandService voiceAsrCommandService;
    private final VoiceApiAssembler assembler;

    public VoiceAsrFacadeImpl(
            VoiceAsrCommandService voiceAsrCommandService,
            VoiceApiAssembler assembler
    ) {
        this.voiceAsrCommandService = voiceAsrCommandService;
        this.assembler = assembler;
    }

    @Override
    public VoiceAsrRecognizeResponse recognizeOnce(Long userId, String profileCode, MultipartFile file, String sourceModule) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("音频文件不能为空");
        }
        try {
            return assembler.toRecognizeResponse(voiceAsrCommandService.recognizeOnce(new VoiceRecognizeOnceCommand(
                    userId,
                    profileCode,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes(),
                    sourceModule
            )));
        } catch (IOException exception) {
            throw new ServiceException("读取上传音频失败", exception);
        }
    }
}