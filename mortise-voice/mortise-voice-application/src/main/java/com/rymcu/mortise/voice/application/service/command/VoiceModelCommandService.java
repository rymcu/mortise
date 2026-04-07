package com.rymcu.mortise.voice.application.service.command;

import com.rymcu.mortise.voice.application.command.VoiceModelUpsertCommand;

/**
 * 语音模型命令服务。
 */
public interface VoiceModelCommandService {

    Boolean createModel(VoiceModelUpsertCommand command);

    Boolean updateModel(Long id, VoiceModelUpsertCommand command);

    Boolean deleteModel(Long id);

    Boolean updateModelStatus(Long id, Integer status);
}