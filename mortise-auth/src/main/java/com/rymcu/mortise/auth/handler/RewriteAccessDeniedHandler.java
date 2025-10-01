package com.rymcu.mortise.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 访问拒绝处理器
 * <p>
 * 当用户已认证但没有权限访问资源时的处理逻辑
 *
 * @author ronger
 */
@Slf4j
@Component
public class RewriteAccessDeniedHandler implements AccessDeniedHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("访问被拒绝: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        ServletOutputStream outputStream = response.getOutputStream();
        GlobalResult<Object> result = GlobalResult.error(ResultCode.UNAUTHORIZED);
        outputStream.write(objectMapper.writeValueAsBytes(result));
        outputStream.flush();
        outputStream.close();
    }
}
