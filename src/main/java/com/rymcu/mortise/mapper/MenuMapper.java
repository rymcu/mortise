package com.rymcu.mortise.mapper;

import com.rymcu.mortise.core.mapper.Mapper;
import com.rymcu.mortise.entity.Menu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2024/4/17 9:43.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
public interface MenuMapper extends Mapper<Menu> {
    List<Menu> selectMenuListByIdRole(@Param("idRole") Long idRole);

    List<Menu> selectMenuListByIdUser(@Param("idUser") Long idUser);

    List<Menu> selectMenuListByIdUserAndParentId(@Param("idUser") Long idUser, @Param("parentId") Long parentId);

    List<Menu> selectMenuListByParentId(@Param("parentId") Long parentId);

    List<Menu> selectMenuListByLabelAndParentId(@Param("label") String label, @Param("parentId") Long parentId);

    int updateStatusByIdMenu(@Param("idMenu") Long idMenu, @Param("status") Integer status);

    int updateDelFlag(@Param("idMenu") Long idMenu, @Param("delFlag") Integer delFlag);
}