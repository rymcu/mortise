package com.rymcu.mortise.auth.handler;

import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.service.AuthCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;

/**
 * OAuth2 登录失败处理器
 * <p>
 * 根据不同的错误类型返回详细的错误信息，帮助用户和开发者定位问题
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/10/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    
    private final AuthCacheService authCacheService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("===============================================");
        log.error("!!! OAuth2 登录失败 !!!");
        log.error("Request URI: {}", request.getRequestURI());
        log.error("异常类型: {}", exception.getClass().getName());
        log.error("异常消息: {}", exception.getMessage());
        log.error("===============================================", exception);

        String state = request.getParameter("state");

        // 更新为取消状态
        if (StringUtils.isNotBlank(state)) {
            authCacheService.storeOAuth2QrcodeState(
                    state,
                    QrcodeState.CANCELED.getValue()
            );
            log.info("OAuth2 登录失败，state: {}", state);
        }

        // 解析错误信息
        OAuth2ErrorInfo errorInfo = parseException(exception);

        // 返回结构化的错误响应
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorInfo.httpStatus());
        response.getWriter().write(String.format(
                "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\",\"detail\":\"%s\"}",
                errorInfo.errorCode(),
                errorInfo.message(),
                escapeJson(errorInfo.detail())
        ));
    }

    /**
     * 解析异常并返回结构化的错误信息
     *
     * @param exception 认证异常
     * @return 错误信息
     */
    private OAuth2ErrorInfo parseException(AuthenticationException exception) {
        // 处理 OAuth2 特定异常
        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            return parseOAuth2Error(oauth2Ex.getError(), exception.getMessage());
        }

        // 处理网络连接异常
        if (exception.getCause() instanceof ConnectException) {
            return new OAuth2ErrorInfo(
                    "provider_unavailable",
                    "OAuth2 服务商暂时不可用，请稍后重试",
                    exception.getMessage(),
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );
        }

        // 处理凭证未找到异常
        if (exception instanceof AuthenticationCredentialsNotFoundException) {
            return new OAuth2ErrorInfo(
                    "credentials_not_found",
                    "认证凭证未找到，请重新发起登录",
                    exception.getMessage(),
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }

        // 处理认证服务异常
        if (exception instanceof AuthenticationServiceException) {
            return new OAuth2ErrorInfo(
                    "service_error",
                    "认证服务异常，请稍后重试",
                    exception.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }

        // 默认错误处理
        return new OAuth2ErrorInfo(
                "authentication_failed",
                "OAuth2 登录失败",
                exception.getMessage(),
                HttpServletResponse.SC_UNAUTHORIZED
        );
    }

    /**
     * 解析 OAuth2 错误码并返回友好的错误信息
     *
     * @param error   OAuth2 错误对象
     * @param rawMessage 原始错误消息
     * @return 错误信息
     */
    private OAuth2ErrorInfo parseOAuth2Error(OAuth2Error error, String rawMessage) {
        String errorCode = error.getErrorCode();
        String description = error.getDescription();

        String message = switch (errorCode) {
            case "invalid_token" -> "Token 无效或已过期，请重新登录";
            case "access_denied" -> "用户拒绝授权";
            case "invalid_request" -> "请求参数错误，请检查授权请求";
            case "invalid_client" -> "OAuth2 客户端配置错误，请联系管理员";
            case "invalid_grant" -> "授权码无效或已过期，请重新发起登录";
            case "unauthorized_client" -> "客户端未授权使用此授权类型";
            case "unsupported_grant_type" -> "不支持的授权类型";
            case "invalid_scope" -> "请求的权限范围无效";
            case "server_error" -> "OAuth2 服务商内部错误，请稍后重试";
            case "temporarily_unavailable" -> "OAuth2 服务商暂时不可用，请稍后重试";
            case "invalid_token_response" -> "获取 Token 失败，服务商返回异常";
            case "invalid_user_info_response" -> "获取用户信息失败，服务商返回异常";
            case "missing_user_info_uri", "invalid_user_info_uri" -> "OAuth2 配置错误：用户信息 URI 无效";
            default -> "OAuth2 认证失败: " + (description != null ? description : errorCode);
        };

        int httpStatus = switch (errorCode) {
            case "server_error", "temporarily_unavailable" -> HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            case "invalid_client", "unauthorized_client" -> HttpServletResponse.SC_FORBIDDEN;
            default -> HttpServletResponse.SC_UNAUTHORIZED;
        };

        return new OAuth2ErrorInfo(errorCode, message, rawMessage, httpStatus);
    }

    /**
     * 转义 JSON 特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * OAuth2 错误信息记录
     */
    private record OAuth2ErrorInfo(String errorCode, String message, String detail, int httpStatus) {
    }
}
