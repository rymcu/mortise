package com.rymcu.mortise.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created on 2024/4/17 9:43.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

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

    /**
     * 根据用户ID查询菜单列表（通过用户角色关联）
     */
    @Select("SELECT id, label, permission FROM mortise_menu tm " +
            "WHERE del_flag = 0 " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_role_menu trm " +
            "  WHERE trm.id_mortise_menu = tm.id " +
            "  AND EXISTS (" +
            "    SELECT 1 FROM mortise_user_role tur " +
            "    WHERE tur.id_mortise_role = trm.id_mortise_role " +
            "    AND tur.id_mortise_user = #{idUser}" +
            "  )" +
            ")")
    List<Menu> findMenusByIdUser(@Param("idUser") Long idUser);

    /**
     * 根据用户ID和父菜单ID查询菜单链接（树形结构）
     */
    @Select("SELECT id, label, permission, parent_id, sort_no, menu_type, icon, href " +
            "FROM mortise_menu tm " +
            "WHERE del_flag = 0 " +
            "AND menu_type in (0, 1) " +
            "AND parent_id = #{parentId} " +
            "AND EXISTS (" +
            "  SELECT 1 FROM mortise_role_menu trm " +
            "  WHERE trm.id_mortise_menu = tm.id " +
            "  AND EXISTS (" +
            "    SELECT 1 FROM mortise_user_role tur " +
            "    WHERE tur.id_mortise_role = trm.id_mortise_role " +
            "    AND tur.id_mortise_user = #{idUser}" +
            "  )" +
            ") " +
            "ORDER BY sort_no ASC")
    List<Menu> findLinksByUserIdAndParentId(@Param("idUser") Long idUser, @Param("parentId") Long parentId);

}
