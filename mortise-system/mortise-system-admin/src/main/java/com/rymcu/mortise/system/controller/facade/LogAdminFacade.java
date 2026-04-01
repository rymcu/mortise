package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.vo.ApiLogVO;
import com.rymcu.mortise.system.controller.vo.OperationLogVO;
import com.rymcu.mortise.system.model.LogSearch;

public interface LogAdminFacade {

    GlobalResult<PageResult<OperationLogVO>> listOperationLogs(LogSearch search);

    GlobalResult<Boolean> deleteOperationLog(Long id);

    GlobalResult<PageResult<ApiLogVO>> listApiLogs(LogSearch search);

    GlobalResult<Boolean> deleteApiLog(Long id);
}
