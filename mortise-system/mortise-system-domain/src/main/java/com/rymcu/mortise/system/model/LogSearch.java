package com.rymcu.mortise.system.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * 日志查询条件
 *
 * @author ronger
 */
@Getter
@Setter
public class LogSearch extends BaseSearch {

    /**
     * 客户端类型：system-后台管理, app-App端, web-Web端, api-开放API
     */
    private String clientType;

    /**
     * 模块名称（用于操作日志过滤）
     */
    private String module;

    /**
     * 操作类型（用于操作日志过滤）
     */
    private String operation;

    /**
     * 是否成功（null=全部, true=成功, false=失败）
     */
    private Boolean success;
}
