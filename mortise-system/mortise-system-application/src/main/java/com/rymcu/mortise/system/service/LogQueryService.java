package com.rymcu.mortise.system.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.persistence.log.entity.ApiLog;
import com.rymcu.mortise.persistence.log.entity.OperationLog;
import com.rymcu.mortise.system.model.LogSearch;

/**
 * 日志查询服务接口
 * 提供操作日志和 API 日志的分页查询与删除功能
 *
 * @author ronger
 */
public interface LogQueryService {

    /**
     * 分页查询操作日志
     *
     * @param page   分页参数
     * @param search 查询条件
     * @return 操作日志分页结果
     */
    Page<OperationLog> findOperationLogs(Page<OperationLog> page, LogSearch search);

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     * @return 是否成功
     */
    Boolean deleteOperationLog(Long id);

    /**
     * 分页查询 API 日志
     *
     * @param page   分页参数
     * @param search 查询条件
     * @return API 日志分页结果
     */
    Page<ApiLog> findApiLogs(Page<ApiLog> page, LogSearch search);

    /**
     * 删除 API 日志
     *
     * @param id 日志ID
     * @return 是否成功
     */
    Boolean deleteApiLog(Long id);
}
