package com.rymcu.mortise.agent.application.service.command.ai.impl;

import com.rymcu.mortise.agent.application.command.AiModelUpsertCommand;
import com.rymcu.mortise.agent.application.service.command.ai.AiModelCommandService;
import com.rymcu.mortise.agent.entity.AiModel;
import com.rymcu.mortise.agent.repository.AiModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 模型命令服务实现。
 */
@Service
public class AiModelCommandServiceImpl implements AiModelCommandService {

    private final AiModelRepository aiModelRepository;

    public AiModelCommandServiceImpl(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createModel(AiModelUpsertCommand command) {
        return aiModelRepository.save(toEntity(command, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateModel(Long id, AiModelUpsertCommand command) {
        return aiModelRepository.update(toEntity(command, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteModel(Long id) {
        return aiModelRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateModelStatus(Long id, Integer status) {
        return aiModelRepository.updateStatus(id, status);
    }

    private AiModel toEntity(AiModelUpsertCommand command, Long id) {
        AiModel model = new AiModel();
        model.setId(id);
        model.setProviderId(command.providerId());
        model.setModelName(command.modelName());
        model.setDisplayName(command.displayName());
        model.setStatus(command.status());
        model.setSortNo(command.sortNo());
        model.setRemark(command.remark());
        return model;
    }
}
