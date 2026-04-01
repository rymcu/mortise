package com.rymcu.mortise.system.entity;

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
@Data
public class DictType implements Serializable {
    /**
     * 主键
     */
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
    private Integer delFlag;
    /**
     * 创建人
     */
    private Long createdBy;
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    /**
     * 更新人
     */
    private Long updatedBy;
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
