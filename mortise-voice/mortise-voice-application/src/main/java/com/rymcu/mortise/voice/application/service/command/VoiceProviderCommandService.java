package com.rymcu.mortise.voice.application.service.command;

import com.rymcu.mortise.voice.application.command.VoiceProviderUpsertCommand;

/**
 * 语音提供商命令服务。
 */
public interface VoiceProviderCommandService {

    Boolean createProvider(VoiceProviderUpsertCommand command);

    Boolean updateProvider(Long id, VoiceProviderUpsertCommand command);

    Boolean deleteProvider(Long id);

    Boolean updateProviderStatus(Long id, Integer status);
}