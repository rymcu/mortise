package com.rymcu.mortise.voice.api.facade;

import com.rymcu.mortise.voice.api.contract.response.VoiceAsrRecognizeResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端短音频识别门面。
 */
public interface VoiceAsrFacade {

    VoiceAsrRecognizeResponse recognizeOnce(Long userId, String profileCode, MultipartFile file, String sourceModule);
}