package com.rymcu.mortise.agent.admin.facade.impl;

import com.rymcu.mortise.agent.admin.assembler.AdminAiModelAssembler;
import com.rymcu.mortise.agent.admin.contract.query.AiModelSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiModelUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiModelInfo;
import com.rymcu.mortise.agent.admin.facade.AdminAiModelFacade;
import com.rymcu.mortise.agent.application.service.command.ai.AiModelCommandService;
import com.rymcu.mortise.agent.application.service.query.ai.AiModelQueryService;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import org.springframework.stereotype.Service;

/**
 * AI 模型管理门面实现。
 */
@Service
public class AdminAiModelFacadeImpl implements AdminAiModelFacade {

    private final AiModelQueryService aiModelQueryService;
    private final AiModelCommandService aiModelCommandService;
    private final AdminAiModelAssembler assembler;

    public AdminAiModelFacadeImpl(
            AiModelQueryService aiModelQueryService,
            AiModelCommandService aiModelCommandService,
            AdminAiModelAssembler assembler
    ) {
        this.aiModelQueryService = aiModelQueryService;
        this.aiModelCommandService = aiModelCommandService;
        this.assembler = assembler;
    }

    @Override
    public PageResult<AiModelInfo> findModelList(PageQuery pageQuery, AiModelSearch search) {
        return aiModelQueryService.findModels(pageQuery, assembler.toSearchQuery(search)).map(assembler::toInfo);
    }

    @Override
    public AiModelInfo findModelInfoById(Long id) {
        return assembler.toInfo(aiModelQueryService.findById(id));
    }

    @Override
    public Boolean createModel(AiModelUpsertRequest request) {
        return aiModelCommandService.createModel(assembler.toCommand(request));
    }

    @Override
    public Boolean updateModel(Long id, AiModelUpsertRequest request) {
        return aiModelCommandService.updateModel(id, assembler.toCommand(request));
    }

    @Override
    public Boolean deleteModel(Long id) {
        return aiModelCommandService.deleteModel(id);
    }

    @Override
    public Boolean enableModel(Long id) {
        return updateStatus(id, Status.ENABLED.getCode());
    }

    @Override
    public Boolean disableModel(Long id) {
        return updateStatus(id, Status.DISABLED.getCode());
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        return aiModelCommandService.updateModelStatus(id, status);
    }
}
