package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rymcu.mortise.entity.OperateLog;
import com.rymcu.mortise.mapper.OperateLogMapper;
import com.rymcu.mortise.model.OperateLogSearch;
import com.rymcu.mortise.service.OperateLogService;
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
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {

    @Override
    public List<OperateLog> findOperateLogs(OperateLogSearch operateLogSearch) {
        return baseMapper.selectOperateLogs(operateLogSearch.getBizNo(), operateLogSearch.getType(), operateLogSearch.getSubType(), operateLogSearch.getStartDate(), operateLogSearch.getEndDate());
    }
}
