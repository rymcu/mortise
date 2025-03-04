package com.rymcu.mortise.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created on 2025/2/24 19:51.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
@Component
public class JwtAuthenticationEntryPoint  implements AuthenticationEntryPoint {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ServletOutputStream outputStream = response.getOutputStream();
        GlobalResult<Object> result = GlobalResultGenerator.genResult(HttpServletResponse.SC_UNAUTHORIZED, null, authException.getMessage());
        outputStream.write(objectMapper.writeValueAsBytes(result));
        outputStream.flush();
        outputStream.close();
    }

}
