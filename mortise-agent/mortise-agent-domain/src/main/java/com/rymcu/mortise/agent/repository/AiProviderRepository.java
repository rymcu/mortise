package com.rymcu.mortise.agent.repository;

import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.model.AiProviderSearchCriteria;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

import java.util.List;

/**
 * AI 提供商仓储端口。
 */
public interface AiProviderRepository {

    List<AiProvider> listEnabledProviders();

    PageResult<AiProvider> findProviders(PageQuery pageQuery, AiProviderSearchCriteria criteria);

    AiProvider findById(Long id);

    boolean save(AiProvider provider);

    boolean update(AiProvider provider);

    boolean deleteById(Long id);

    boolean updateStatus(Long id, Integer status);
}
