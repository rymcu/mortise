package com.rymcu.mortise.agent.repository;

import com.rymcu.mortise.agent.entity.AiModel;
import com.rymcu.mortise.agent.model.AiModelSearchCriteria;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

import java.util.List;

/**
 * AI 模型仓储端口。
 */
public interface AiModelRepository {

    List<AiModel> listEnabledModelsByProviderId(Long providerId);

    PageResult<AiModel> findModels(PageQuery pageQuery, AiModelSearchCriteria criteria);

    AiModel findById(Long id);

    boolean save(AiModel model);

    boolean update(AiModel model);

    boolean deleteById(Long id);

    boolean updateStatus(Long id, Integer status);
}
