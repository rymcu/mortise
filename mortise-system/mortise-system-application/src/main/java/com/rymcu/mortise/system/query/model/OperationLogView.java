package com.rymcu.mortise.system.query.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志读模型。
 */
@Data
public class OperationLogView implements Serializable {

    private Long id;
    private String traceId;
    private String clientType;
    private String module;
    private String operation;
    private Long operatorId;
    private String operatorAccount;
    private LocalDateTime operateTime;
    private String method;
    private String requestUri;
    private String requestMethod;
    private String params;
    private String result;
    private String ip;
    private String userAgent;
    private Long duration;
    private Boolean success;
    private String errorMsg;
    private LocalDateTime createdTime;
}
