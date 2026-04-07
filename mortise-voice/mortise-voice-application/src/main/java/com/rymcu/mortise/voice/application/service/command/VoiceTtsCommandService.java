package com.rymcu.mortise.voice.application.service.command;

import com.rymcu.mortise.voice.application.command.VoiceSynthesizeCommand;
import com.rymcu.mortise.voice.application.result.VoiceSynthesizeResult;

/**
 * 同步语音合成命令服务。
 */
public interface VoiceTtsCommandService {

    VoiceSynthesizeResult synthesize(VoiceSynthesizeCommand command);
}