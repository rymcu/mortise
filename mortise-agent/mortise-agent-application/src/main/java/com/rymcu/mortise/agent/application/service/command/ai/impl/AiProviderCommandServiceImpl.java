package com.rymcu.mortise.agent.application.service.command.ai.impl;

import com.rymcu.mortise.agent.application.command.AiProviderUpsertCommand;
import com.rymcu.mortise.agent.application.service.command.ai.AiProviderCommandService;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.repository.AiProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 提供商命令服务实现。
 */
@Service
public class AiProviderCommandServiceImpl implements AiProviderCommandService {

    private final AiProviderRepository aiProviderRepository;

    public AiProviderCommandServiceImpl(AiProviderRepository aiProviderRepository) {
        this.aiProviderRepository = aiProviderRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProvider(AiProviderUpsertCommand command) {
        return aiProviderRepository.save(toEntity(command, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProvider(Long id, AiProviderUpsertCommand command) {
        return aiProviderRepository.update(toEntity(command, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProvider(Long id) {
        return aiProviderRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProviderStatus(Long id, Integer status) {
        return aiProviderRepository.updateStatus(id, status);
    }

    private AiProvider toEntity(AiProviderUpsertCommand command, Long id) {
        AiProvider provider = new AiProvider();
        provider.setId(id);
        provider.setName(command.name());
        provider.setCode(command.code());
        provider.setApiKey(command.apiKey());
        provider.setBaseUrl(command.baseUrl());
        provider.setDefaultModelName(command.defaultModelName());
        provider.setStatus(command.status());
        provider.setSortNo(command.sortNo());
        provider.setRemark(command.remark());
        return provider;
    }
}
