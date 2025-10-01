package com.rymcu.mortise.log.service;

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
}
