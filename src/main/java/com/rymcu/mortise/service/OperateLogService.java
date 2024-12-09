package com.rymcu.mortise.service;

import com.rymcu.mortise.core.service.Service;
import com.rymcu.mortise.model.OperateLogSearch;
import com.rymcu.mortise.entity.OperateLog;

import java.util.List;

/**
 * Created on 2024/3/13 9:48.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface OperateLogService extends Service<OperateLog> {
    List<OperateLog> findOperateLogs(OperateLogSearch operateLogSearch);
}
