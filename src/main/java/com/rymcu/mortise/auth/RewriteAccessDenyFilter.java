package com.rymcu.mortise.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.core.result.ResultCode;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created on 2025/2/25 8:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
@Component
public class RewriteAccessDenyFilter implements AccessDeniedHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ServletOutputStream outputStream = response.getOutputStream();
        GlobalResult<Object> result = GlobalResultGenerator.genErrorResult(ResultCode.UNAUTHORIZED);
        outputStream.write(objectMapper.writeValueAsBytes(result));
        outputStream.flush();
        outputStream.close();
    }
}
