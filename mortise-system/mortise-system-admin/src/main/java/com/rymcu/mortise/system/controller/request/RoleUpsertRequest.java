package com.rymcu.mortise.system.controller.request;

import lombok.Data;

/**
 * 角色新增/更新请求。
 */
@Data
public class RoleUpsertRequest {

    private String label;
    private String permission;
    private Integer status;
    private Integer isDefault;
}
