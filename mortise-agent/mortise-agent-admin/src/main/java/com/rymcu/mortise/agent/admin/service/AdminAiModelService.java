package com.rymcu.mortise.agent.admin.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.agent.admin.model.AiModelInfo;
import com.rymcu.mortise.agent.admin.model.AiModelSearch;
import com.rymcu.mortise.agent.service.AiModelService;

/**
 * AI 模型管理服务接口
 *
 * @author ronger
 */
public interface AdminAiModelService extends AiModelService {

    /**
     * 分页查询模型列表
     */
    Page<AiModelInfo> findModelList(Page<AiModelInfo> page, AiModelSearch search);

    /**
     * 根据 ID 查询模型信息
     */
    AiModelInfo findModelInfoById(Long id);

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
