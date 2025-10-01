package com.rymcu.mortise.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.core.result.GlobalResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证成功处理器
 *
 * @author ronger
 */
@Slf4j
@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        
        // 生成 JWT Token
        String token = jwtTokenUtil.generateToken(username);

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);

        // 返回成功响应
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                objectMapper.writeValueAsString(GlobalResult.success(data))
        );

        log.info("用户登录成功: {}", username);
    }
}
