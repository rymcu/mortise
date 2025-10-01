package com.rymcu.mortise.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.core.result.GlobalResult;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证入口点
 * <p>
 * 当用户未认证就访问受保护资源时的处理逻辑
 *
 * @author ronger
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.warn("未认证访问: {} - {}", request.getRequestURI(), authException.getMessage());
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ServletOutputStream outputStream = response.getOutputStream();
        GlobalResult<Object> result = GlobalResult.error(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        outputStream.write(objectMapper.writeValueAsBytes(result));
        outputStream.flush();
        outputStream.close();
    }
}
