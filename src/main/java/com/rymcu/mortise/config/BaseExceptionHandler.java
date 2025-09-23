package com.rymcu.mortise.config;

import com.rymcu.mortise.core.exception.BusinessException;
import com.rymcu.mortise.core.exception.CaptchaException;
import com.rymcu.mortise.core.exception.RateLimitException;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.security.auth.login.AccountException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author ronger
 */
@RestControllerAdvice
public class BaseExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    /**
     * 处理业务逻辑异常 (ServiceException)
     * 这类异常由业务代码主动抛出，代表一个已知的业务错误。
     * HTTP 状态码通常为 400 (Bad Request) 或其他 4xx。
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleServiceException(ServiceException ex) {
        logger.warn("业务逻辑异常: {}", ex.getMessage());
        return GlobalResult.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理认证和授权相关异常
     * 包括凭证错误、账户不存在、权限不足等。
     * 为避免账户枚举攻击，对客户端返回统一的模糊提示。
     * HTTP 状态码为 401 (Unauthorized)。
     */
    @ExceptionHandler({AccountException.class, BadCredentialsException.class, UsernameNotFoundException.class, CaptchaException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public GlobalResult<Void> handleAuthenticationException(Exception ex) {
        // 对于安全类异常，只记录警告信息，但对客户端返回统一的错误提示，防止泄露过多信息
        logger.warn("认证失败: {}", ex.getMessage());
        if(ex instanceof CaptchaException) {
            return GlobalResult.error(ResultCode.INVALID_VERIFICATION_CODE);
        }
        return GlobalResult.error(ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD);
    }

    /**
     * 处理用户权限不足异常 (Spring Security)
     * HTTP 状态码为 403 (Forbidden)
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public GlobalResult<Void> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {
        logger.warn("权限不足: 用户尝试访问受限资源 [{}], {}", request.getRequestURI(), ex.getMessage());
        return GlobalResult.error(ResultCode.UNAUTHORIZED);
    }

    /**
     * 处理限流异常 (RateLimitException)
     * HTTP 状态码为 429 (Too Many Requests)
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public GlobalResult<Void> handleRateLimitException(RateLimitException ex, HttpServletRequest request) {
        logger.warn("API限流触发: 客户端IP [{}] 访问 [{}] 超过限制", 
                    Utils.getIpAddress(), request.getRequestURI());
        return GlobalResult.error(429, ex.getMessage());
    }

    /**
     * 处理参数校验相关的异常
     * 如 BusinessException 或 IllegalArgumentException，通常表示客户端传递了非法参数。
     * HTTP 状态码为 400 (Bad Request)。
     */
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleValidationException(Exception ex) {
        logger.warn("参数校验失败: {}", ex.getMessage());
        return GlobalResult.error(ResultCode.INVALID_PARAM.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常 (MethodArgumentNotValidException)
     * 通常由 @RequestBody 的参数校验失败触发
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String message = String.join(", ", errorMessages);
        logger.warn("请求参数校验失败: {}", message);
        return GlobalResult.error(ResultCode.INVALID_PARAM.getCode(), message);
    }

    /**
     * 处理表单参数校验异常 (BindException)
     * 通常由表单提交的参数校验失败触发
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<Void> handleBindException(BindException ex) {
        List<String> errorMessages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String message = String.join(", ", errorMessages);
        logger.warn("表单参数校验失败: {}", message);
        return GlobalResult.error(ResultCode.INVALID_PARAM.getCode(), message);
    }

    /**
     * 处理资源未找到异常
     * HTTP 状态码为 404 (Not Found)。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GlobalResult<Void> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("资源未找到: 无法为请求 [{}] 找到处理器", request.getRequestURI());
        return GlobalResult.error(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底异常处理器：处理所有未被上述处理器捕获的异常
     * 这是最后的防线，应记录为ERROR级别，并对客户端返回统一的“服务器内部错误”信息。
     * HTTP 状态码为 500 (Internal Server Error)。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<Void> handleUncaughtException(Exception ex, HttpServletRequest request, Object handler) {
        // 记录详细的错误日志，包括请求信息和异常堆栈
        logError(ex, request, handler);
        // 对客户端屏蔽内部细节，返回统一的错误信息
        return GlobalResult.error(ResultCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 记录详细的错误日志
     * @param ex 异常对象
     * @param request HTTP请求
     * @param handler 处理器 (可能为null)
     */
    private void logError(Exception ex, HttpServletRequest request, Object handler) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        String logMessage;
        if (handler instanceof HandlerMethod handlerMethod) {
            String controller = handlerMethod.getBean().getClass().getName();
            String controllerMethod = handlerMethod.getMethod().getName();
            logMessage = String.format("未捕获异常: 请求 [%s %s] 在 [%s#%s] 中发生错误.",
                    method, requestUri, controller, controllerMethod);
        } else {
            logMessage = String.format("未捕获异常: 请求 [%s %s] 发生未知位置的错误.", 
                    method, requestUri);
        }
        // 使用ERROR级别记录，并附带完整的异常堆栈
        logger.error(logMessage, ex);
    }
}
