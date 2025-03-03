package com.rymcu.mortise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rymcu.mortise.entity.OperateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2024/4/13 21:20.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLog> {
    List<OperateLog> selectOperateLogs(@Param("bizNo") String bizNo, @Param("type") String type, @Param("subType") String subType, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
