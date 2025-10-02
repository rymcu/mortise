package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper
 *
 * @author ronger
 * @since 2025-10-02
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
