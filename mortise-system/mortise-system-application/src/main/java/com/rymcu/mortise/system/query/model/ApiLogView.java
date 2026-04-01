package com.rymcu.mortise.system.query.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API 日志读模型。
 */
@Data
public class ApiLogView implements Serializable {

    private Long id;
    private String traceId;
    private String clientType;
    private String apiDescription;
    private String className;
    private String methodName;
    private Long userId;
    private String username;
    private LocalDateTime requestTime;
    private String requestUri;
    private String requestMethod;
    private String queryString;
    private String requestHeaders;
    private String requestBody;
    private String responseBody;
    private Integer httpStatus;
    private String clientIp;
    private String userAgent;
    private Long duration;
    private Boolean success;
    private String errorMsg;
    private LocalDateTime createdTime;
}
