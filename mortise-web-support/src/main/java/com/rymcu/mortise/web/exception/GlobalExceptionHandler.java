package com.rymcu.mortise.web.exception;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
        return GlobalResult.error(ResultCode.BAD_REQUEST, e.getMessage());
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
     * 处理数据库唯一约束异常
     * <p>
     * 当数据库的唯一索引或主键冲突时，Spring会抛出此异常。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict: 请求冲突，通常由于资源已存在。
    public GlobalResult<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据重复，违反唯一约束: {}", e.getMessage(), e);

        // 基础友好的错误提示
        String userMessage = "记录已存在，请勿重复提交。";

        // 【进阶】尝试从异常信息中提取更具体的提示
        //  注意：这种方式依赖于特定数据库的错误消息格式，可能不够健壮，但能提供更好的用户体验。
        //  PostgreSQL的错误信息: "violates unique constraint "..."
        //  详细：Key (module_id, sort_no)=(...) already exists.
        String rootCauseMessage = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        if (rootCauseMessage != null) {
            if (rootCauseMessage.contains("sort_no")) {
                userMessage = "该模块下已存在相同的排序号，请使用其他排序号。";
            }
        }

        // 建议在 ResultCode 中定义一个专门用于数据冲突的枚举
        return GlobalResult.error(ResultCode.DATA_CONFLICT, userMessage);
    }

    /**
     * 处理404错误 - 找不到请求的资源
     * <p>
     * 需要配置 spring.mvc.throw-exception-if-no-handler-found=true
     * 和 spring.web.resources.add-mappings=false (仅在不需要默认静态资源映射时)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GlobalResult<Void> handleNotFoundException(NoHandlerFoundException e) {
        log.warn("请求的资源不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
        return GlobalResult.error(ResultCode.NOT_FOUND, "请求的资源不存在");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GlobalResult<Void> handleNotResourceFoundException(NoResourceFoundException e) {
        log.warn("请求的资源不存在: {} {}", e.getHttpMethod(), e.getResourcePath());
        return GlobalResult.error(ResultCode.NOT_FOUND, "请求的资源不存在");
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
