package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper
 *
 * @author ronger
 * @since 2025-10-02
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    /**
     * 根据角色ID查询用户列表
     */
    @Select("SELECT id, nickname, account, email FROM mortise_user tu " +
            "WHERE del_flag = 0 " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_user_role tur " +
            "  WHERE tur.id_mortise_user = tu.id " +
            "  AND tur.id_mortise_role = #{idRole}" +
            ")")
    List<User> findUsersByIdRole(@Param("idRole") Long idRole);
    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT id, label, permission FROM mortise_role tr " +
            "WHERE del_flag = 0 " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_user_role tur " +
            "  WHERE tur.id_mortise_role = tr.id " +
            "  AND tur.id_mortise_user = #{idUser}" +
            ")")
    List<Role> findRolesByIdUser(@Param("idUser") Long idUser);
}
