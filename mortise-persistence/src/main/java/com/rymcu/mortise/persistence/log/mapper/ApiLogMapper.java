package com.rymcu.mortise.persistence.log.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.persistence.log.entity.ApiLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * API日志 Mapper
 *
 * @author ronger
 */
@Mapper
public interface ApiLogMapper extends BaseMapper<ApiLog> {

}
