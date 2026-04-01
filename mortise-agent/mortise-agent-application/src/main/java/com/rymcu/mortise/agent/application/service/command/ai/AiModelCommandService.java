package com.rymcu.mortise.agent.application.service.command.ai;

import com.rymcu.mortise.agent.application.command.AiModelUpsertCommand;

/**
 * AI 模型命令服务。
 */
public interface AiModelCommandService {

    Boolean createModel(AiModelUpsertCommand command);

    Boolean updateModel(Long id, AiModelUpsertCommand command);

    Boolean deleteModel(Long id);

    Boolean updateModelStatus(Long id, Integer status);
}
