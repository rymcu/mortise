package com.rymcu.mortise.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Created on 2024/4/17 8:56.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Data
@TableName(value = "mortise_menu", schema = "mortise")
public class Menu {

    @TableId(value = "id")
    private Long idMenu;

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
     * 删除标记
     */
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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;
}
