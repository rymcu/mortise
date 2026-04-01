package com.rymcu.mortise.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端字典类型视图对象。
 */
@Data
public class DictTypeVO {

    private Long id;
    private String label;
    private String typeCode;
    private String description;
    private Integer sortNo;
    private Integer status;
    private Integer delFlag;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
