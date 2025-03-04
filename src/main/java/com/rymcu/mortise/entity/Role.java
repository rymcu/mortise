package com.rymcu.mortise.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ronger
 */
@Data
@TableName(value = "mortise_role", schema = "mortise")
public class Role implements Serializable {

    @TableId(value = "id")
    private Long idRole;

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
    private Integer status;
    /**
     * 删除标记
     */
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
