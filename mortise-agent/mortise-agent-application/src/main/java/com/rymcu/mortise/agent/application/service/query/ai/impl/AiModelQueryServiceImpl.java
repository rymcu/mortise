package com.rymcu.mortise.agent.application.service.query.ai.impl;

import com.rymcu.mortise.agent.application.query.AiModelSearchQuery;
import com.rymcu.mortise.agent.application.result.AiModelResult;
import com.rymcu.mortise.agent.application.service.query.ai.AiModelQueryService;
import com.rymcu.mortise.agent.entity.AiModel;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.model.AiModelSearchCriteria;
import com.rymcu.mortise.agent.repository.AiModelRepository;
import com.rymcu.mortise.agent.repository.AiProviderRepository;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 模型查询服务实现。
 */
@Service
public class AiModelQueryServiceImpl implements AiModelQueryService {

    private final AiModelRepository aiModelRepository;
    private final AiProviderRepository aiProviderRepository;

    public AiModelQueryServiceImpl(
            AiModelRepository aiModelRepository,
            AiProviderRepository aiProviderRepository
    ) {
        this.aiModelRepository = aiModelRepository;
        this.aiProviderRepository = aiProviderRepository;
    }

    @Override
    public List<AiModelResult> listEnabledModelsByProviderId(Long providerId) {
        return aiModelRepository.listEnabledModelsByProviderId(providerId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public PageResult<AiModelResult> findModels(PageQuery pageQuery, AiModelSearchQuery criteria) {
        return aiModelRepository.findModels(pageQuery, toCriteria(criteria)).map(this::toResult);
    }

    @Override
    public AiModelResult findById(Long id) {
        return toResult(aiModelRepository.findById(id));
    }

    private AiModelSearchCriteria toCriteria(AiModelSearchQuery query) {
        if (query == null) {
            return null;
        }
        return new AiModelSearchCriteria(
                query.providerId(),
                query.modelName(),
                query.query(),
                query.status()
        );
    }

    private AiModelResult toResult(AiModel model) {
        if (model == null) {
            return null;
        }
        return new AiModelResult(
                model.getId(),
                model.getProviderId(),
                resolveProviderName(model.getProviderId()),
                model.getModelName(),
                model.getDisplayName(),
                model.getStatus(),
                model.getSortNo(),
                model.getRemark(),
                model.getCreatedTime(),
                model.getUpdatedTime()
        );
    }

    private String resolveProviderName(Long providerId) {
        if (providerId == null) {
            return null;
        }
        AiProvider provider = aiProviderRepository.findById(providerId);
        return provider == null ? null : provider.getName();
    }
}
