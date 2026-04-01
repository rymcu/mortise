package com.rymcu.mortise.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端字典视图对象。
 */
@Data
public class DictVO {

    private Long id;
    private String dictTypeCode;
    private String label;
    private String value;
    private Integer sortNo;
    private Integer status;
    private Integer delFlag;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    private String icon;
    private String image;
    private String color;
}
