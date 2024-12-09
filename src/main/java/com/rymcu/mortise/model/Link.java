package com.rymcu.mortise.model;

import lombok.Data;

import java.util.List;

/**
 * Created on 2024/4/17 9:38.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class Link {

    private Long id;

    /**
     * 菜单名称
     */
    private String label;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单链接
     */
    private String to;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 排序
     */
    private Integer sortNo;
    /**
     * 父级菜单主键
     */
    private Long parentId;

    private Tooltip tooltip;

    private List<Link> children;
}
