package com.rymcu.mortise.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.system.annotation.DictFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ronger
 */
@Data
@Table(value = "mortise_role", schema = "mortise")
public class Role implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
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
     * 删除标记
     */
    @DictFormat(value = "DelFlag")
    @Column(isLogicDelete = true)
    private Integer delFlag;

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
