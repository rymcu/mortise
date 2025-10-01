package com.rymcu.mortise.log.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author ronger
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogEntity {

    /**
     * 日志ID
     */
    private String id;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人账号
     */
    private String operatorAccount;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 返回结果
     */
    private String result;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMsg;
}
