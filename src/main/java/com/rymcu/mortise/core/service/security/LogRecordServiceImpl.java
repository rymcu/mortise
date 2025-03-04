package com.rymcu.mortise.core.service.security;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.mzt.logapi.beans.CodeVariableType;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import com.rymcu.mortise.entity.OperateLog;
import com.rymcu.mortise.mapper.OperateLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Created on 2024/2/27 14:14.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.core.service.security
 */
@Slf4j
@Service
public class LogRecordServiceImpl implements ILogRecordService {

    @Resource
    private OperateLogMapper operateLogMapper;

    @Override
    public void record(LogRecord logRecord) {
        OperateLog operateLog = genOperateLog(logRecord);
        String javaMethod = logRecord.getCodeVariable().get(CodeVariableType.ClassName) + "#" + logRecord.getCodeVariable().get(CodeVariableType.MethodName);
        operateLog.setJavaMethod(javaMethod);
        operateLog.setTraceId(UlidCreator.getUlid().toString());
        operateLogMapper.insert(operateLog);
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        return null;
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        return null;
    }



    private static OperateLog genOperateLog(LogRecord logRecord) {
        OperateLog operateLog = new OperateLog();
        operateLog.setTenant(logRecord.getTenant());
        operateLog.setType(logRecord.getType());
        operateLog.setSubType(logRecord.getSubType());
        operateLog.setBizNo(logRecord.getBizNo());
        operateLog.setOperator(logRecord.getOperator());
        operateLog.setContent(logRecord.getAction());
        operateLog.setFail(logRecord.isFail());
        operateLog.setExtra(logRecord.getExtra());
        operateLog.setCreatedTime(DateUtil.toLocalDateTime(logRecord.getCreateTime()));
        return operateLog;
    }
}
