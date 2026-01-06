package com.rymcu.mortise.persistence.log.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志数据库实体
 *
 * @author ronger
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "mortise_operation_log", schema = "mortise")
public class OperationLog implements Serializable {

    /**
     * 日志ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 客户端类型
     */
    private String clientType;

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
     * 请求方法签名
     */
    private String method;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求方式 (GET/POST/PUT/DELETE)
     */
    private String requestMethod;

    /**
     * 请求参数 (JSON)
     */
    private String params;

    /**
     * 返回结果 (JSON)
     */
    private String result;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 用户代理
     */
    private String userAgent;

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

    /**
     * 创建时间
     */
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
