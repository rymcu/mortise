package com.rymcu.mortise.log.resolver;

import com.rymcu.mortise.common.constant.ClientTypeConstant;
import com.rymcu.mortise.common.constant.HttpHeaderConstant;
import com.rymcu.mortise.log.spi.ClientTypeResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认客户端类型解析器
 * <p>
 * 解析优先级：
 * 1. 优先使用 nginx 传递的 X-Client-Type 请求头
 * 2. 其次使用 X-Original-URI（nginx 代理前的原始路径）
 * 3. 最后根据实际请求 URI 判断
 * <p>
 * URL 路径匹配规则：
 * - /api/admin/**, /admin/**, /system/** → system
 * - /api/app/**, /app/**, /api/v1/app/**, /api/v2/app/** → app
 * - /api/v1/**, /api/v2/**, /open/** → api
 * - /web/**, /api/web/** → web
 *
 * @author ronger
 */
@Slf4j
@Component
public class DefaultClientTypeResolver implements ClientTypeResolver {

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // 最低优先级，作为兜底
    }

    @Override
    public boolean supports(HttpServletRequest request) {
        // 默认解析器总是支持
        return true;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        // 1. 优先使用 nginx 传递的 X-Client-Type
        String clientType = request.getHeader(HttpHeaderConstant.X_CLIENT_TYPE);
        if (clientType != null && !clientType.isEmpty()) {
            return ClientTypeConstant.normalize(clientType);
        }

        // 2. 尝试使用 nginx 传递的原始 URI
        String uri = request.getHeader(HttpHeaderConstant.X_ORIGINAL_URI);
        if (uri == null || uri.isEmpty()) {
            uri = request.getRequestURI();
        }

        if (uri == null) {
            return CLIENT_TYPE_UNKNOWN;
        }

        return resolveByUri(uri, request);
    }

    /**
     * 根据 URI 路径判断客户端类型
     */
    private String resolveByUri(String uri, HttpServletRequest request) {
        // 后台管理系统
        if (uri.startsWith("/api/admin") || uri.startsWith("/admin") || uri.startsWith("/system")) {
            return CLIENT_TYPE_SYSTEM;
        }

        // App 端（支持 nginx 重写后的路径: /api/app -> /api/v1/app）
        if (uri.startsWith("/api/app") || uri.startsWith("/app") 
                || uri.startsWith("/api/v1/app") || uri.startsWith("/api/v2/app")) {
            return CLIENT_TYPE_APP;
        }

        // Web 前端
        if (uri.startsWith("/web") || uri.startsWith("/api/web")
                || uri.startsWith("/api/v1/web") || uri.startsWith("/api/v2/web")) {
            return CLIENT_TYPE_WEB;
        }

        // 开放 API（如 /api/v1/pay 等，放在 app/web 判断之后）
        if (uri.startsWith("/api/v1") || uri.startsWith("/api/v2") || uri.startsWith("/open")) {
            return CLIENT_TYPE_API;
        }

        // 默认按 User-Agent 判断
        return resolveByUserAgent(request);
    }

    /**
     * 根据 User-Agent 判断客户端类型
     */
    private String resolveByUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            // 移动端 App
            if (userAgent.contains("mortise-app") || userAgent.contains("okhttp") || userAgent.contains("retrofit")) {
                return CLIENT_TYPE_APP;
            }
            // 小程序
            if (userAgent.contains("miniprogram") || userAgent.contains("wechatdevtools")) {
                return CLIENT_TYPE_APP;
            }
        }
        // 默认返回 API
        return CLIENT_TYPE_API;
    }
}
