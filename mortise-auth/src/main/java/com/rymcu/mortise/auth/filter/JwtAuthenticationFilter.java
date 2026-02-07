package com.rymcu.mortise.auth.filter;

import com.rymcu.mortise.auth.resolver.UserTypeResolverChain;
import com.rymcu.mortise.auth.service.CustomUserDetailsService;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 认证过滤器（支持多用户表）
 * <p>
 * 拦截请求，验证 JWT Token 并设置认证信息
 * </p>
 * <p>
 * 自动发现所有 CustomUserDetailsService 实现，并根据请求路径选择对应的服务
 * </p>
 *
 * @author ronger
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private TokenManager tokenManager;

    @Resource
    private UserTypeResolverChain userTypeResolverChain;

    /**
     * 所有 CustomUserDetailsService 实现
     * <p>
     * Spring 会自动注入所有实现了 CustomUserDetailsService 接口的 Bean
     * </p>
     */
    private final List<CustomUserDetailsService> userDetailsServices;

    /**
     * 用户类型与 UserDetailsService 的映射缓存
     * <p>
     * key: 用户类型（如 "system", "member"）
     * value: 对应的 CustomUserDetailsService
     * </p>
     */
    private final Map<String, CustomUserDetailsService> serviceCache = new ConcurrentHashMap<>();

    /**
     * 构造函数注入（自动发现所有 CustomUserDetailsService）
     *
     * @param userDetailsServices 所有用户详情服务实现
     */
    public JwtAuthenticationFilter(List<CustomUserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;

        if (userDetailsServices == null || userDetailsServices.isEmpty()) {
            log.warn("未发现任何 CustomUserDetailsService 实现，JWT 认证可能无法正常工作");
        } else {
            log.info("JWT 过滤器发现 {} 个 CustomUserDetailsService 实现", userDetailsServices.size());
            userDetailsServices.forEach(service ->
                log.info("  - {}", service.getClass().getSimpleName())
            );
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 获取 Token
        String authToken = extractToken(request);

        if (authToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 检查 Token 是否已被注销（黑名单检查）
                if (tokenManager.isTokenRevoked(authToken)) {
                    log.debug("Token 已被注销，拒绝认证");
                    chain.doFilter(request, response);
                    return;
                }

                // 从 Token 中获取用户名
                String username = jwtTokenUtil.getUsernameFromToken(authToken);

                if (username != null) {
                    // 使用 UserTypeResolverChain 确定用户类型
                    String userType = userTypeResolverChain.resolve(request);
                    CustomUserDetailsService userDetailsService = selectUserDetailsService(userType);

                    if (userDetailsService != null) {
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

                            log.debug("JWT 认证成功: {} (用户类型: {}, 服务: {})",
                                    username,
                                    userType,
                                    userDetailsService.getClass().getSimpleName());
                        }
                    } else {
                        log.warn("未找到支持用户类型 '{}' 的 UserDetailsService", userType);
                    }
                }
            } catch (UsernameNotFoundException e) {
                log.debug("JWT 认证失败: 用户不存在 - {}", e.getMessage());
            } catch (Exception e) {
                log.error("JWT 认证失败", e);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 根据用户类型选择对应的 UserDetailsService
     * <p>
     * 使用缓存提高性能，避免每次都遍历所有服务
     * </p>
     *
     * @param userType 用户类型标识
     * @return 对应的 CustomUserDetailsService，如果未找到则返回 null
     */
    private CustomUserDetailsService selectUserDetailsService(String userType) {
        // 先从缓存中查找
        if (serviceCache.containsKey(userType)) {
            return serviceCache.get(userType);
        }

        // 遍历所有服务，找到支持该用户类型的服务
        for (CustomUserDetailsService service : userDetailsServices) {
            if (service.supports(userType)) {
                // 找到后放入缓存
                serviceCache.put(userType, service);
                log.debug("为用户类型 '{}' 选择服务: {}", userType, service.getClass().getSimpleName());
                return service;
            }
        }

        log.warn("未找到支持用户类型 '{}' 的 UserDetailsService", userType);
        return null;
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtTokenUtil.getTokenHeader());
        if (headerValue == null) {
            return null;
        }

        // 兼容：
        // 1) 正常格式: "Bearer <jwt>"
        // 2) 重复前缀: "Bearer Bearer <jwt>"（前端把 tokenType 拼进 token 后又加了一次）
        // 3) 多余空格/大小写差异
        String token = headerValue.trim();

        String prefix = jwtTokenUtil.getTokenPrefix();
        String normalizedPrefix = prefix == null ? "Bearer" : prefix.trim();
        if (normalizedPrefix.isEmpty()) {
            normalizedPrefix = "Bearer";
        }

        // 持续剥离前缀，直到不再以 Bearer 开头
        while (token.length() > normalizedPrefix.length()
                && token.regionMatches(true, 0, normalizedPrefix, 0, normalizedPrefix.length())) {
            token = token.substring(normalizedPrefix.length()).trim();
        }

        return token.isEmpty() ? null : token;
    }
}
