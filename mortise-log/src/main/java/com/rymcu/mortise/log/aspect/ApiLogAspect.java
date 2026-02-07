package com.rymcu.mortise.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.entity.ApiLogEntity;
import com.rymcu.mortise.log.resolver.ClientTypeResolverChain;
import com.rymcu.mortise.log.service.LogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API 日志切面
 * 用于记录 API 调用日志（请求、响应、耗时等）
 *
 * @author ronger
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 敏感字段列表，这些字段的值将被脱敏
     */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "pwd", "secret", "token", "accessToken", "refreshToken",
            "authorization", "credential", "privateKey", "apiKey"
    );

    @Resource
    private LogService logService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ClientTypeResolverChain clientTypeResolverChain;

    @Around("@annotation(apiLog)")
    public Object around(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 构建 API 日志实体
        ApiLogEntity logEntity = ApiLogEntity.builder()
                .apiDescription(apiLog.value())
                .className(signature.getDeclaringTypeName())
                .methodName(signature.getName())
                .requestTime(LocalDateTime.now())
                .build();

        // 自动填充操作人信息
        fillOperatorInfo(logEntity);

        // 获取请求信息
        fillRequestInfo(logEntity, apiLog);

        // 获取链路追踪ID
        fillTraceId(logEntity);

        // 记录请求体
        if (apiLog.recordRequestBody()) {
            recordRequestBody(joinPoint, logEntity);
        }

        Object result = null;
        boolean success = true;
        String errorMsg = null;
        Integer httpStatus = 200;

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录响应体
            if (apiLog.recordResponseBody() && result != null) {
                try {
                    String responseBody = objectMapper.writeValueAsString(result);
                    // 限制响应体大小，避免日志过大
                    if (responseBody.length() > 10000) {
                        responseBody = responseBody.substring(0, 10000) + "...(truncated)";
                    }
                    logEntity.setResponseBody(responseBody);
                } catch (Exception e) {
                    log.warn("序列化响应体失败", e);
                }
            }
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            httpStatus = 500;
            throw e;
        } finally {
            // 记录执行结果
            long duration = System.currentTimeMillis() - startTime;
            logEntity.setDuration(duration);
            logEntity.setSuccess(success);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setHttpStatus(httpStatus);

            // 异步保存日志
            logService.recordApiLogAsync(logEntity);
        }

        return result;
    }

    /**
     * 自动填充操作人信息
     */
    private void fillOperatorInfo(ApiLogEntity logEntity) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof CurrentUser currentUser) {
                    logEntity.setUserId(currentUser.getUserId());
                    logEntity.setUsername(currentUser.getUsername());
                } else if (principal instanceof String username) {
                    if (!"anonymousUser".equals(username)) {
                        logEntity.setUsername(username);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取操作人信息失败: {}", e.getMessage());
        }
    }

    /**
     * 填充请求信息
     */
    private void fillRequestInfo(ApiLogEntity logEntity, ApiLog apiLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setRequestUri(request.getRequestURI());
                logEntity.setRequestMethod(request.getMethod());
                logEntity.setClientIp(getIpAddress(request));
                logEntity.setUserAgent(request.getHeader("User-Agent"));
                if (apiLog.recordParams()) {
                    logEntity.setQueryString(request.getQueryString());
                }
                // 自动识别客户端类型
                logEntity.setClientType(clientTypeResolverChain.resolve(request));

                // 记录请求头
                if (apiLog.recordHeaders()) {
                    logEntity.setRequestHeaders(getFilteredHeaders(request));
                }
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }
    }

    /**
     * 填充链路追踪ID
     */
    private void fillTraceId(ApiLogEntity logEntity) {
        try {
            String traceId = MDC.get(TRACE_ID_KEY);
            if (traceId != null) {
                logEntity.setTraceId(traceId);
            }
        } catch (Exception e) {
            log.debug("获取 traceId 失败", e);
        }
    }

    /**
     * 记录请求体
     */
    private void recordRequestBody(ProceedingJoinPoint joinPoint, ApiLogEntity logEntity) {
        try {
            Object[] args = filterArgs(joinPoint.getArgs());
            if (args != null && args.length > 0) {
                String requestBody = objectMapper.writeValueAsString(args);
                // 脱敏处理
                requestBody = desensitize(requestBody);
                // 限制大小
                if (requestBody.length() > 10000) {
                    requestBody = requestBody.substring(0, 10000) + "...(truncated)";
                }
                logEntity.setRequestBody(requestBody);
            }
        } catch (Exception e) {
            log.warn("序列化请求体失败", e);
        }
    }

    /**
     * 获取过滤后的请求头（排除敏感头）
     */
    private String getFilteredHeaders(HttpServletRequest request) {
        try {
            Map<String, String> headers = new LinkedHashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String lowerName = name.toLowerCase();
                // 排除敏感请求头
                if (!SENSITIVE_FIELDS.contains(lowerName) && !lowerName.contains("authorization")) {
                    headers.put(name, request.getHeader(name));
                } else {
                    headers.put(name, "***");
                }
            }
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            log.warn("序列化请求头失败", e);
            return null;
        }
    }

    /**
     * 过滤不可序列化的参数
     */
    private Object[] filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        return Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .filter(arg -> !(arg instanceof HttpServletResponse))
                .filter(arg -> !(arg instanceof MultipartFile))
                .filter(arg -> !(arg instanceof InputStream))
                .filter(arg -> !(arg instanceof OutputStream))
                .filter(arg -> !(arg instanceof Authentication))
                .filter(arg -> !(arg instanceof UserDetails))
                .collect(Collectors.toList())
                .toArray();
    }

    /**
     * 敏感字段脱敏处理
     * 简单实现：将敏感字段值替换为 ***
     */
    private String desensitize(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        String result = json;
        for (String field : SENSITIVE_FIELDS) {
            // 匹配 "field":"value" 或 "field": "value" 格式
            result = result.replaceAll(
                    "\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                    "\"" + field + "\":\"***\""
            );
        }
        return result;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
