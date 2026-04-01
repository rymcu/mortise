package com.rymcu.mortise.agent.admin.facade;

import com.rymcu.mortise.agent.admin.contract.query.AiProviderSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiProviderUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiProviderInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

/**
 * AI 提供商管理门面。
 */
public interface AdminAiProviderFacade {

    PageResult<AiProviderInfo> findProviderList(PageQuery pageQuery, AiProviderSearch search);

    /**
     * 根据 ID 查询提供商信息
     */
    AiProviderInfo findProviderInfoById(Long id);

    /**
     * 创建提供商。
     */
    Boolean createProvider(AiProviderUpsertRequest request);

    /**
     * 更新提供商。
     */
    Boolean updateProvider(Long id, AiProviderUpsertRequest request);

    /**
     * 删除提供商。
     */
    Boolean deleteProvider(Long id);

    /**
     * 启用提供商
     */
    Boolean enableProvider(Long id);

    /**
     * 禁用提供商
     */
    Boolean disableProvider(Long id);

    /**
     * 更新提供商状态
     */
    Boolean updateStatus(Long id, Integer status);
}
