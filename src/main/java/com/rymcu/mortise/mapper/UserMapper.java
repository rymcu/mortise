package com.rymcu.mortise.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * Created on 2024/4/13 15:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    Set<String> selectUserRolePermissionsByIdUser(@Param("idUser") Long idUser);

}
