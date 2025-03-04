package com.rymcu.mortise.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created on 2024/9/22 19:55.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@TableName(value = "mortise_dict", schema = "mortise")
@Data
public class Dict {

    /**
     * 主键
     */
    @TableId(value = "id")
    @TableField(value = "id")
    private Long idDict;
    /**
     * 代码
     */
    private String dictTypeCode;
    /**
     * 名称
     */
    private String label;
    /**
     * 数据
     */
    private String value;
    /**
     * 排序
     */
    private Integer sortNo;
    /**
     * 状态
     */
    private Integer status;
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
