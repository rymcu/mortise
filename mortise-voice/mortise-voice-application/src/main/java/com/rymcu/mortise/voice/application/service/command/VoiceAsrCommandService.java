package com.rymcu.mortise.voice.application.service.command;

import com.rymcu.mortise.voice.application.command.VoiceRecognizeOnceCommand;
import com.rymcu.mortise.voice.application.result.VoiceRecognizeOnceResult;

/**
 * 短音频识别命令服务。
 */
public interface VoiceAsrCommandService {

    VoiceRecognizeOnceResult recognizeOnce(VoiceRecognizeOnceCommand command);
}