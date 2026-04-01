package com.rymcu.mortise.agent.application.service.query.ai;

import com.rymcu.mortise.agent.application.query.AiModelSearchQuery;
import com.rymcu.mortise.agent.application.result.AiModelResult;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

import java.util.List;

/**
 * AI 模型查询服务。
 */
public interface AiModelQueryService {

    List<AiModelResult> listEnabledModelsByProviderId(Long providerId);

    PageResult<AiModelResult> findModels(PageQuery pageQuery, AiModelSearchQuery criteria);

    AiModelResult findById(Long id);
}
