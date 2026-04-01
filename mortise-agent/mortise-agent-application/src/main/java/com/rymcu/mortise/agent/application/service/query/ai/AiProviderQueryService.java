package com.rymcu.mortise.agent.application.service.query.ai;

import com.rymcu.mortise.agent.application.query.AiProviderSearchQuery;
import com.rymcu.mortise.agent.application.result.AiProviderResult;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

import java.util.List;

/**
 * AI 提供商查询服务。
 */
public interface AiProviderQueryService {

    List<AiProviderResult> listEnabledProviders();

    PageResult<AiProviderResult> findProviders(PageQuery pageQuery, AiProviderSearchQuery criteria);

    AiProviderResult findById(Long id);
}
