package com.rymcu.mortise.agent.admin.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.agent.admin.model.AiProviderInfo;
import com.rymcu.mortise.agent.admin.model.AiProviderSearch;
import com.rymcu.mortise.agent.service.AiProviderService;

/**
 * AI 提供商管理服务接口
 *
 * @author ronger
 */
public interface AdminAiProviderService extends AiProviderService {

    /**
     * 分页查询提供商列表
     */
    Page<AiProviderInfo> findProviderList(Page<AiProviderInfo> page, AiProviderSearch search);

    /**
     * 根据 ID 查询提供商信息
     */
    AiProviderInfo findProviderInfoById(Long id);

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
