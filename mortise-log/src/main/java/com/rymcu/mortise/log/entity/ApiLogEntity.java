package com.rymcu.mortise.log.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 日志实体
 *
 * @author ronger
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogEntity {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 客户端类型: system-后台管理, app-App端, web-Web端, api-开放API
     */
    private String clientType;

    /**
     * API 描述
     */
    private String apiDescription;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求方式 (GET/POST/PUT/DELETE)
     */
    private String requestMethod;

    /**
     * 查询参数
     */
    private String queryString;

    /**
     * 请求头（JSON格式）
     */
    private String requestHeaders;

    /**
     * 请求体（JSON格式）
     */
    private String requestBody;

    /**
     * 响应体（JSON格式）
     */
    private String responseBody;

    /**
     * HTTP 状态码
     */
    private Integer httpStatus;

    /**
     * 客户端IP
     */
    private String clientIp;

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
}
