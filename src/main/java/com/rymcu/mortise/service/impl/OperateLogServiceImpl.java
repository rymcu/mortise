package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.core.service.AbstractService;
import com.rymcu.mortise.model.OperateLogSearch;
import com.rymcu.mortise.entity.OperateLog;
import com.rymcu.mortise.mapper.OperateLogMapper;
import com.rymcu.mortise.service.OperateLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created on 2024/3/13 9:48.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class OperateLogServiceImpl extends AbstractService<OperateLog> implements OperateLogService {

    @Resource
    private OperateLogMapper operateLogMapper;

    @Override
    public List<OperateLog> findOperateLogs(OperateLogSearch operateLogSearch) {
        return operateLogMapper.selectOperateLogs(operateLogSearch.getBizNo(), operateLogSearch.getType(), operateLogSearch.getSubType(), operateLogSearch.getStartDate(), operateLogSearch.getEndDate());
    }
}
