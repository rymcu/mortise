package com.rymcu.mortise.agent.application.service.query.ai.impl;

import com.rymcu.mortise.agent.application.query.AiProviderSearchQuery;
import com.rymcu.mortise.agent.application.result.AiProviderResult;
import com.rymcu.mortise.agent.application.service.query.ai.AiProviderQueryService;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.model.AiProviderSearchCriteria;
import com.rymcu.mortise.agent.repository.AiProviderRepository;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 提供商查询服务实现。
 */
@Service
public class AiProviderQueryServiceImpl implements AiProviderQueryService {

    private final AiProviderRepository aiProviderRepository;

    public AiProviderQueryServiceImpl(AiProviderRepository aiProviderRepository) {
        this.aiProviderRepository = aiProviderRepository;
    }

    @Override
    public List<AiProviderResult> listEnabledProviders() {
        return aiProviderRepository.listEnabledProviders().stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public PageResult<AiProviderResult> findProviders(PageQuery pageQuery, AiProviderSearchQuery criteria) {
        return aiProviderRepository.findProviders(pageQuery, toCriteria(criteria)).map(this::toResult);
    }

    @Override
    public AiProviderResult findById(Long id) {
        return toResult(aiProviderRepository.findById(id));
    }

    private AiProviderSearchCriteria toCriteria(AiProviderSearchQuery query) {
        if (query == null) {
            return null;
        }
        return new AiProviderSearchCriteria(
                query.name(),
                query.code(),
                query.query(),
                query.status()
        );
    }

    private AiProviderResult toResult(AiProvider provider) {
        if (provider == null) {
            return null;
        }
        return new AiProviderResult(
                provider.getId(),
                provider.getName(),
                provider.getCode(),
                provider.getApiKey(),
                provider.getBaseUrl(),
                provider.getDefaultModelName(),
                provider.getStatus(),
                provider.getSortNo(),
                provider.getRemark(),
                provider.getCreatedTime(),
                provider.getUpdatedTime()
        );
    }
}
