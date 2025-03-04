package com.rymcu.mortise.config;

import com.rymcu.mortise.core.exception.BusinessException;
import com.rymcu.mortise.core.exception.CaptchaException;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.view.json.AbstractJackson2View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.security.auth.login.AccountException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author ronger
 */
@RestControllerAdvice
public class BaseExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object errorHandler(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (isAjax(request)) {
            GlobalResult<ResultCode> result = new GlobalResult<>();
            if (ex instanceof BadCredentialsException) {
                result = new GlobalResult<>(ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD);
                logger.info("账号或密码错误");
            } else if (ex instanceof AuthorizationDeniedException) {
                result = new GlobalResult<>(ResultCode.UNAUTHORIZED);
                logger.info("用户无权限");
            } else if (ex instanceof UsernameNotFoundException) {
                // 账号或密码错误
                result = new GlobalResult<>(ResultCode.UNKNOWN_ACCOUNT);
                logger.info(ex.getMessage());
            }  else if (ex instanceof CaptchaException) {
                // 验证码错误
                result = new GlobalResult<>(ResultCode.VALIDATE);
                logger.info(ex.getMessage());
            } else if (ex instanceof AccountException) {
                // 账号或密码错误
                result = new GlobalResult<>(ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD);
                logger.info(ex.getMessage());
            } else if (ex instanceof ServiceException) {
                //业务失败的异常，如“账号或密码错误”
                result.setCode(((ServiceException) ex).getCode());
                result.setMessage(ex.getMessage());
                logger.info(ex.getMessage());
            } else if (ex instanceof NoHandlerFoundException) {
                result.setCode(ResultCode.NOT_FOUND.getCode());
                result.setMessage(ResultCode.NOT_FOUND.getMessage());
            } else if (ex instanceof ServletException) {
                result.setCode(ResultCode.FAIL.getCode());
                result.setMessage(ex.getMessage());
            } else if (ex instanceof BusinessException || ex instanceof IllegalArgumentException) {
                result.setCode(ResultCode.INVALID_PARAM.getCode());
                result.setMessage(ex.getMessage());
            } else {
                //系统内部异常,不返回给客户端,内部记录错误日志
                result = new GlobalResult<>(ResultCode.INTERNAL_SERVER_ERROR);
                printExceptionMessage(request, handler, ex);
            }
            return result;
        } else {
            ModelAndView mv = new ModelAndView();
            AbstractJackson2View view = new MappingJackson2JsonView();
            Map<String, Object> attributes = new HashMap<>(2);
            switch (ex) {
                case BadCredentialsException unauthenticatedException -> {
                    attributes.put("code", ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD.getCode());
                    attributes.put("message", ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD.getMessage());
                }
                case AuthorizationDeniedException unauthorizedException -> {
                    attributes.put("code", ResultCode.UNAUTHORIZED.getCode());
                    attributes.put("message", ResultCode.UNAUTHORIZED.getMessage());
                }
                case UsernameNotFoundException serviceException -> {
                    //业务失败的异常，如“账号或密码错误”
                    attributes.put("code", ResultCode.UNKNOWN_ACCOUNT.getCode());
                    attributes.put("message", ex.getMessage());
                    logger.info(ex.getMessage());
                }
                case ServiceException serviceException -> {
                    //业务失败的异常，如“账号或密码错误”
                    attributes.put("code", serviceException.getCode());
                    attributes.put("message", ex.getMessage());
                    logger.info(ex.getMessage());
                }
                case NoHandlerFoundException noHandlerFoundException -> {
                    attributes.put("code", ResultCode.NOT_FOUND.getCode());
                    attributes.put("message", ResultCode.NOT_FOUND.getMessage());
                }
                case ServletException servletException -> {
                    attributes.put("code", ResultCode.FAIL.getCode());
                    attributes.put("message", ex.getMessage());
                }
                case BusinessException businessException -> {
                    attributes.put("code", ResultCode.INVALID_PARAM.getCode());
                    attributes.put("message", ex.getMessage());
                }
                case CaptchaException captchaException -> {
                    attributes.put("code", ResultCode.VALIDATE.getCode());
                    attributes.put("message", ex.getMessage());
                }
                case null, default -> {
                    //系统内部异常,不返回给客户端,内部记录错误日志
                    attributes.put("code", ResultCode.INTERNAL_SERVER_ERROR.getCode());
                    attributes.put("message", ResultCode.INTERNAL_SERVER_ERROR.getMessage());
                    printExceptionMessage(request, handler, ex);
                }
            }
            view.setAttributesMap(attributes);
            mv.setView(view);
            return mv;
        }
    }

    private void printExceptionMessage(HttpServletRequest request, Object handler, Exception ex) {
        String message;
        if (handler instanceof HandlerMethod handlerMethod) {
            message = String.format("接口 [%s] 出现异常，方法：%s.%s，异常摘要：%s",
                    request.getRequestURI(),
                    handlerMethod.getBean().getClass().getName(),
                    handlerMethod.getMethod().getName(),
                    ex.getMessage());
        } else {
            message = ex.getMessage();
        }
        logger.error(message, ex);
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }
        String contentType = request.getContentType();
        return StringUtils.isNotBlank(contentType) && contentType.contains("application/json");
    }
}
