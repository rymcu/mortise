package com.rymcu.mortise.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.log.entity.OperationLogEntity;
import com.rymcu.mortise.log.service.LogService;
import com.rymcu.mortise.log.resolver.ClientTypeResolverChain;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 操作日志切面
 *
 * @author ronger
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private static final String TRACE_ID_KEY = "traceId";

    @Resource
    private LogService logService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ClientTypeResolverChain clientTypeResolverChain;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 构建日志实体
        OperationLogEntity logEntity = OperationLogEntity.builder()
                .module(operationLog.module())
                .operation(operationLog.operation())
                .operateTime(LocalDateTime.now())
                .method(joinPoint.getSignature().toLongString())
                .build();

        // 自动填充操作人信息
        fillOperatorInfo(logEntity);

        // 获取请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setIp(getIpAddress(request));
                logEntity.setRequestUri(request.getRequestURI());
                logEntity.setRequestMethod(request.getMethod());
                logEntity.setUserAgent(request.getHeader("User-Agent"));
                // 自动识别客户端类型
                logEntity.setClientType(clientTypeResolverChain.resolve(request));
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }

        // 获取链路追踪ID
        try {
            String traceId = MDC.get(TRACE_ID_KEY);
            if (traceId != null) {
                logEntity.setTraceId(traceId);
            }
        } catch (Exception e) {
            log.debug("获取 traceId 失败", e);
        }

        // 记录请求参数（过滤敏感参数）
        if (operationLog.recordParams()) {
            try {
                Object[] args = filterArgs(joinPoint.getArgs());
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
     * 自动填充操作人信息
     * 从 Spring Security 上下文中获取当前登录用户
     */
    private void fillOperatorInfo(OperationLogEntity logEntity) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                // 优先使用 CurrentUser 接口
                if (principal instanceof CurrentUser currentUser) {
                    logEntity.setOperatorId(currentUser.getUserId());
                    logEntity.setOperatorAccount(currentUser.getUsername());
                } else if (principal instanceof String username) {
                    // 处理匿名用户或简单字符串 principal
                    if (!"anonymousUser".equals(username)) {
                        logEntity.setOperatorAccount(username);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取操作人信息失败: {}", e.getMessage());
        }
    }

    /**
     * 过滤不可序列化的参数
     * 排除 HttpServletRequest、HttpServletResponse、MultipartFile 等类型
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
