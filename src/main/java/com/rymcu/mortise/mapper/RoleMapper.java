package com.rymcu.mortise.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    List<Role> selectRolesByIdUser(@Param("idUser") Long idUser);

    Role selectRoleByPermission(@Param("permission") String permission);

    List<Role> selectRoles(@Param("page") Page<Role> page, @Param("label") String label, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("order") String order, @Param("sort") String sort);

    int insertRoleMenu(@Param("idRole") Long idRole, @Param("idMenu") Long idMenu);

    Set<Long> selectRoleMenus(@Param("idRole") Long idRole);

}
