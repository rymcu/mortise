package com.rymcu.mortise.persistence.log.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.persistence.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author ronger
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
