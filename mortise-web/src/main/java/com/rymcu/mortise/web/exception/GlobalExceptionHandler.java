package com.rymcu.mortise.web.exception;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各类异常并返回规范的响应
 *
 * @author ronger
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return GlobalResult.error(ResultCode.FAIL, e.getMessage());
    }

    /**
     * 处理服务异常
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<Void> handleServiceException(ServiceException e) {
        log.error("服务异常: {}", e.getMessage(), e);
        return GlobalResult.error(ResultCode.SERVER_ERROR, e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public GlobalResult<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        return GlobalResult.error(ResultCode.UNAUTHENTICATED, "认证失败，请重新登录");
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public GlobalResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("授权异常: {}", e.getMessage());
        return GlobalResult.error(ResultCode.UNAUTHORIZED, "没有访问权限");
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数校验失败: {}", errors);
        return GlobalResult.error(ResultCode.INVALID_PARAM, errors);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleBindException(BindException e) {
        String errors = e.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数绑定失败: {}", errors);
        return GlobalResult.error(ResultCode.INVALID_PARAM, errors);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return GlobalResult.error(ResultCode.INVALID_PARAM, e.getMessage());
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return GlobalResult.error(ResultCode.SERVER_ERROR, "系统繁忙，请稍后重试");
    }
}
