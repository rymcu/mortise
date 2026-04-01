package com.rymcu.mortise.system.entity;

import com.rymcu.mortise.system.annotation.DictFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created on 2024/4/17 8:56.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Data
public class Menu implements Serializable {

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
    @DictFormat(value = "Status")
    private Integer status;
    /**
     * 删除标记
     */
    @DictFormat(value = "DelFlag")
    private Integer delFlag;
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

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
