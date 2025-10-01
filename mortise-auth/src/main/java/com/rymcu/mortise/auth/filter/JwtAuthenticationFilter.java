package com.rymcu.mortise.auth.filter;

import com.rymcu.mortise.auth.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT 认证过滤器
 * 拦截请求，验证 JWT Token 并设置认证信息
 *
 * @author ronger
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    private final UserDetailsService userDetailsService;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public JwtAuthenticationFilter(Optional<UserDetailsService> userDetailsServiceOptional) {
        this.userDetailsService = userDetailsServiceOptional.orElse(null);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 获取 Token
        String authToken = extractToken(request);

        if (authToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 从 Token 中获取用户名
                String username = jwtTokenUtil.getUsernameFromToken(authToken);

                if (username != null && userDetailsService != null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 验证 Token
                    if (jwtTokenUtil.validateToken(authToken, username)) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // 设置到安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT 认证成功: {}", username);
                    }
                }
            } catch (Exception e) {
                log.error("JWT 认证失败", e);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtTokenUtil.getTokenHeader());
        if (bearerToken != null && bearerToken.startsWith(jwtTokenUtil.getTokenPrefix())) {
            return bearerToken.substring(jwtTokenUtil.getTokenPrefix().length());
        }
        return null;
    }
}
