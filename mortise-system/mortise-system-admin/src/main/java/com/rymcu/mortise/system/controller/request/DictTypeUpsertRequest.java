package com.rymcu.mortise.system.controller.request;

import lombok.Data;

/**
 * 字典类型新增/更新请求。
 */
@Data
public class DictTypeUpsertRequest {

    private String label;
    private String typeCode;
    private String description;
    private Integer sortNo;
    private Integer status;
}
