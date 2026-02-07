package com.rymcu.mortise.log.service;

import com.rymcu.mortise.log.entity.ApiLogEntity;
import com.rymcu.mortise.log.entity.OperationLogEntity;

/**
 * 日志服务接口
 *
 * @author ronger
 */
public interface LogService {

    /**
     * 记录操作日志
     *
     * @param log 操作日志
     */
    void recordLog(OperationLogEntity log);

    /**
     * 异步记录操作日志
     *
     * @param log 操作日志
     */
    void recordLogAsync(OperationLogEntity log);

    /**
     * 记录 API 日志
     *
     * @param log API日志
     */
    void recordApiLog(ApiLogEntity log);

    /**
     * 异步记录 API 日志
     *
     * @param log API日志
     */
    void recordApiLogAsync(ApiLogEntity log);
}
