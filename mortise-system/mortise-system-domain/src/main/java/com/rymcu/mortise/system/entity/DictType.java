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
 * Created on 2024/9/22 20:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Table(value = "mortise_dict_type", schema = "mortise")
@Data
public class DictType implements Serializable {
    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    /**
     * 名称
     */
    private String label;
    /**
     * 代码
     */
    private String typeCode;
    /**
     * 描述
     */
    private String description;
    /**
     * 排序
     */
    private Integer sortNo;
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
     * 创建人
     */
    private Long createdBy;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    /**
     * 更新人
     */
    private Long updatedBy;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
