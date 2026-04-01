package com.rymcu.mortise.system.controller.request;

import lombok.Data;

/**
 * 菜单新增/更新请求。
 */
@Data
public class MenuUpsertRequest {

    private String label;
    private String permission;
    private String icon;
    private String href;
    private Integer status;
    private Integer menuType;
    private Integer sortNo;
    private Long parentId;
}
