package com.rymcu.mortise.system.entity;

import com.rymcu.mortise.system.annotation.DictFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ronger
 */
@Data
public class Role implements Serializable {

    private Long id;

    /**
     * 角色名称
     */
    private String label;

    /**
     * 角色权限
     */
    private String permission;

    /**
     * 状态
     */
    @DictFormat(value = "Status")
    private Integer status;

    /**
     * 是否为默认角色（注册时自动分配）
     * 0-否，1-是
     * 注意：系统通过数据库唯一索引保证只有一个默认角色
     */
    @DictFormat(value = "DefaultFlag")
    private Integer isDefault;

    /**
     * 删除标记
     */
    @DictFormat(value = "DelFlag")
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
