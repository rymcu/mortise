package com.rymcu.mortise.system.model;

import com.rymcu.mortise.system.annotation.DictFormat;
import lombok.Data;

import java.util.List;

/**
 * Created on 2025/10/2 21:51.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.system.model
 */
@Data
public class MenuTreeInfo {

    private Long id;

    /**
     * 菜单名称
     */
    private String label;

    /**
     * 菜单权限
     */
    private String permission;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单链接
     */
    private String href;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 菜单类型: 0-目录, 1-菜单, 2-按钮
     */
    @DictFormat(value = "MenuType")
    private Integer menuType;

    /**
     * 排序
     */
    private Integer sortNo;
    /**
     * 父级菜单主键
     */
    private Long parentId;

    private List<MenuTreeInfo> children;
}
