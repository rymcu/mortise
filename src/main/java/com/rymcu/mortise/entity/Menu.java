package com.rymcu.mortise.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.rymcu.mortise.annotation.DictFormat;
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
@Table(value = "mortise_menu", schema = "mortise")
public class Menu implements Serializable {

    @Id
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
    @Column(isLogicDelete = true)
    private Integer delFlag;
    /**
     * 类型
     */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
