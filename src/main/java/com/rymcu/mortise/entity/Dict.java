package com.rymcu.mortise.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rymcu.mortise.annotation.DictFormat;
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
    @TableId
    private Long id;
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

    /**
     * 图标
     */
    private String icon;

    /**
     * 图片
     */
    private String image;
    /**
     * chip 颜色
     */
    private String color;

}
