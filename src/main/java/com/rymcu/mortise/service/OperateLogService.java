package com.rymcu.mortise.service;

import com.rymcu.mortise.entity.OperateLog;
import com.rymcu.mortise.model.OperateLogSearch;

import java.util.List;

/**
 * Created on 2024/3/13 9:48.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface OperateLogService {
    List<OperateLog> findOperateLogs(OperateLogSearch operateLogSearch);
}
