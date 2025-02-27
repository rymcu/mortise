package com.rymcu.mortise.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Created on 2024/9/22 20:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@TableName(value = "mortise_dict", schema = "mortise")
@Data
public class DictType {
    /**
     * 主键
     */
    @TableId(value = "id")
    private String id;
    /**
     * 名称
     */
    private String typeName;
    /**
     * 代码
     */
    private String typeCode;
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
    private Date createdTime;
    /**
     * 更新人
     */
    private Long updatedBy;
    /**
     * 更新时间
     */
    private Date updatedTime;
}
