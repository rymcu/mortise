package com.rymcu.mortise.persistence.systemconfig.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.persistence.systemconfig.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通用系统配置 Mapper。
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
}