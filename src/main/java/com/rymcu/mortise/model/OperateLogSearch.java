package com.rymcu.mortise.model;

import lombok.Data;

/**
 * Created on 2024/4/18 11:31.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class OperateLogSearch {
    private String bizNo;
    /**
     * 操作者
     */
    private String operator;
    /**
     * 操作模块
     */
    private String type;

    /**
     * 操作分类
     */
    private String subType;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;
}
