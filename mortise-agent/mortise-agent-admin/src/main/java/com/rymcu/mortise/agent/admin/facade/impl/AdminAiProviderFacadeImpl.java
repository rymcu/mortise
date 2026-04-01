package com.rymcu.mortise.agent.admin.facade.impl;

import com.rymcu.mortise.agent.admin.assembler.AdminAiProviderAssembler;
import com.rymcu.mortise.agent.admin.contract.query.AiProviderSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiProviderUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiProviderInfo;
import com.rymcu.mortise.agent.admin.facade.AdminAiProviderFacade;
import com.rymcu.mortise.agent.application.service.command.ai.AiProviderCommandService;
import com.rymcu.mortise.agent.application.service.query.ai.AiProviderQueryService;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import org.springframework.stereotype.Service;

/**
 * AI 提供商管理门面实现。
 */
@Service
public class AdminAiProviderFacadeImpl implements AdminAiProviderFacade {

    private final AiProviderQueryService aiProviderQueryService;
    private final AiProviderCommandService aiProviderCommandService;
    private final AdminAiProviderAssembler assembler;

    public AdminAiProviderFacadeImpl(
            AiProviderQueryService aiProviderQueryService,
            AiProviderCommandService aiProviderCommandService,
            AdminAiProviderAssembler assembler
    ) {
        this.aiProviderQueryService = aiProviderQueryService;
        this.aiProviderCommandService = aiProviderCommandService;
        this.assembler = assembler;
    }

    @Override
    public PageResult<AiProviderInfo> findProviderList(PageQuery pageQuery, AiProviderSearch search) {
        return aiProviderQueryService.findProviders(pageQuery, assembler.toSearchQuery(search)).map(assembler::toInfo);
    }

    @Override
    public AiProviderInfo findProviderInfoById(Long id) {
        return assembler.toInfo(aiProviderQueryService.findById(id));
    }

    @Override
    public Boolean createProvider(AiProviderUpsertRequest request) {
        return aiProviderCommandService.createProvider(assembler.toCommand(request));
    }

    @Override
    public Boolean updateProvider(Long id, AiProviderUpsertRequest request) {
        return aiProviderCommandService.updateProvider(id, assembler.toCommand(request));
    }

    @Override
    public Boolean deleteProvider(Long id) {
        return aiProviderCommandService.deleteProvider(id);
    }

    @Override
    public Boolean enableProvider(Long id) {
        return updateStatus(id, Status.ENABLED.getCode());
    }

    @Override
    public Boolean disableProvider(Long id) {
        return updateStatus(id, Status.DISABLED.getCode());
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        return aiProviderCommandService.updateProviderStatus(id, status);
    }
}
