package com.rymcu.mortise.agent.application.service.command.ai;

import com.rymcu.mortise.agent.application.command.AiProviderUpsertCommand;

/**
 * AI 提供商命令服务。
 */
public interface AiProviderCommandService {

    Boolean createProvider(AiProviderUpsertCommand command);

    Boolean updateProvider(Long id, AiProviderUpsertCommand command);

    Boolean deleteProvider(Long id);

    Boolean updateProviderStatus(Long id, Integer status);
}
