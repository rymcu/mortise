package com.rymcu.mortise.agent.admin.facade;

import com.rymcu.mortise.agent.admin.contract.query.AiModelSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiModelUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiModelInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;

/**
 * AI 模型管理门面。
 */
public interface AdminAiModelFacade {

    PageResult<AiModelInfo> findModelList(PageQuery pageQuery, AiModelSearch search);

    /**
     * 根据 ID 查询模型信息
     */
    AiModelInfo findModelInfoById(Long id);

    /**
     * 创建模型。
     */
    Boolean createModel(AiModelUpsertRequest request);

    /**
     * 更新模型。
     */
    Boolean updateModel(Long id, AiModelUpsertRequest request);

    /**
     * 删除模型。
     */
    Boolean deleteModel(Long id);

    /**
     * 启用模型
     */
    Boolean enableModel(Long id);

    /**
     * 禁用模型
     */
    Boolean disableModel(Long id);

    /**
     * 更新模型状态
     */
    Boolean updateStatus(Long id, Integer status);
}
