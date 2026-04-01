package com.rymcu.mortise.system.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.model.LogSearch;
import com.rymcu.mortise.system.query.model.ApiLogView;
import com.rymcu.mortise.system.query.model.OperationLogView;

/**
 * 日志读模型查询服务。
 */
public interface LogQueryService {

    PageResult<OperationLogView> findOperationLogs(PageQuery pageQuery, LogSearch search);

    Boolean deleteOperationLog(Long id);

    PageResult<ApiLogView> findApiLogs(PageQuery pageQuery, LogSearch search);

    Boolean deleteApiLog(Long id);
}
