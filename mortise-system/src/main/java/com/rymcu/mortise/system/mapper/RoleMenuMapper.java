package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单关联 Mapper
 *
 * @author ronger
 * @since 2025-10-02
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {


    /**
     * 根据角色ID查询菜单列表
     */
    @Select("SELECT id, label, permission FROM mortise_menu tm " +
            "WHERE del_flag = 0 " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_role_menu trm " +
            "  WHERE trm.id_mortise_menu = tm.id " +
            "  AND trm.id_mortise_role = #{idRole}" +
            ")")
    List<Menu> findMenusByIdRole(@Param("idRole") Long idRole);

}
