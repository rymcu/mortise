package com.rymcu.mortise.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端菜单视图对象。
 */
@Data
public class MenuVO {

    private Long id;
    private String label;
    private String permission;
    private String icon;
    private String href;
    private Integer status;
    private Integer delFlag;
    private Integer menuType;
    private Integer sortNo;
    private Long parentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
