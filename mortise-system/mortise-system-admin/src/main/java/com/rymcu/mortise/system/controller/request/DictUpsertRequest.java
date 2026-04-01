package com.rymcu.mortise.system.controller.request;

import lombok.Data;

/**
 * 字典新增/更新请求。
 */
@Data
public class DictUpsertRequest {

    private String dictTypeCode;
    private String label;
    private String value;
    private Integer sortNo;
    private Integer status;
    private String icon;
    private String image;
    private String color;
}
