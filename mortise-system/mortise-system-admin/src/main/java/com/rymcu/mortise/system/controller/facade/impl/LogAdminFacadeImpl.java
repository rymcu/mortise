package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.assembler.LogAdminAssembler;
import com.rymcu.mortise.system.controller.facade.LogAdminFacade;
import com.rymcu.mortise.system.controller.vo.ApiLogVO;
import com.rymcu.mortise.system.controller.vo.OperationLogVO;
import com.rymcu.mortise.system.model.LogSearch;
import com.rymcu.mortise.system.query.LogQueryService;
import org.springframework.stereotype.Component;

@Component
public class LogAdminFacadeImpl implements LogAdminFacade {

    private final LogQueryService logQueryService;

    public LogAdminFacadeImpl(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @Override
    public GlobalResult<PageResult<OperationLogVO>> listOperationLogs(LogSearch search) {
        return GlobalResult.success(
                logQueryService.findOperationLogs(PageQuery.of(search.getPageNum(), search.getPageSize()), search)
                        .map(LogAdminAssembler::toOperationLogVO)
        );
    }

    @Override
    public GlobalResult<Boolean> deleteOperationLog(Long id) {
        return GlobalResult.success(logQueryService.deleteOperationLog(id));
    }

    @Override
    public GlobalResult<PageResult<ApiLogVO>> listApiLogs(LogSearch search) {
        return GlobalResult.success(
                logQueryService.findApiLogs(PageQuery.of(search.getPageNum(), search.getPageSize()), search)
                        .map(LogAdminAssembler::toApiLogVO)
        );
    }

    @Override
    public GlobalResult<Boolean> deleteApiLog(Long id) {
        return GlobalResult.success(logQueryService.deleteApiLog(id));
    }
}
