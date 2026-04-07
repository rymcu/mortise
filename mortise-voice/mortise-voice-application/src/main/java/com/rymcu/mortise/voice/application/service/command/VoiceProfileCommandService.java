package com.rymcu.mortise.voice.application.service.command;

import com.rymcu.mortise.voice.application.command.VoiceProfileUpsertCommand;

/**
 * 语音配置命令服务。
 */
public interface VoiceProfileCommandService {

    Boolean createProfile(VoiceProfileUpsertCommand command);

    Boolean updateProfile(Long id, VoiceProfileUpsertCommand command);

    Boolean deleteProfile(Long id);

    Boolean updateProfileStatus(Long id, Integer status);
}