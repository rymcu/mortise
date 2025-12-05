package com.rymcu.mortise.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.log.entity.OperationLogEntity;
import com.rymcu.mortise.log.service.LogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 *
 * @author ronger
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private LogService logService;

    @Resource
    private ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 构建日志实体
        OperationLogEntity logEntity = OperationLogEntity.builder()
                .id(UlidCreator.getUlid().toString())
                .module(operationLog.module())
                .operation(operationLog.operation())
                .operateTime(LocalDateTime.now())
                .method(joinPoint.getSignature().toLongString())
                .build();

        // 获取请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setIp(getIpAddress(request));
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }

        // 记录请求参数
        if (operationLog.recordParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    logEntity.setParams(objectMapper.writeValueAsString(args));
                }
            } catch (Exception e) {
                log.warn("序列化请求参数失败", e);
            }
        }

        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录返回结果
            if (operationLog.recordResult() && result != null) {
                try {
                    logEntity.setResult(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.warn("序列化返回结果失败", e);
                }
            }
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 记录执行结果
            long duration = System.currentTimeMillis() - startTime;
            logEntity.setDuration(duration);
            logEntity.setSuccess(success);
            logEntity.setErrorMsg(errorMsg);

            // 保存日志
            if (operationLog.async()) {
                logService.recordLogAsync(logEntity);
            } else {
                logService.recordLog(logEntity);
            }
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
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
