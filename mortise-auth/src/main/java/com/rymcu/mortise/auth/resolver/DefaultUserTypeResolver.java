package com.rymcu.mortise.auth.resolver;

import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.spi.UserTypeResolver;
import com.rymcu.mortise.common.constant.ClientTypeConstant;
import com.rymcu.mortise.common.constant.HttpHeaderConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 默认用户类型解析器
 * <p>
 * 提供基于请求头和请求路径的用户类型解析。
 * 优先从请求头 x-client-type 获取用户类型，
 * 如果请求头不存在，则根据请求路径判断。
 * </p>
 *
 * <p>配置示例：</p>
 * <pre>
 * mortise:
 *   auth:
 *     user-type:
 *       header-name: x-client-type
 *       member-paths:
 *         - /api/app
 *         - /api/member
 *         - /api/v1/app
 * </pre>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class DefaultUserTypeResolver implements UserTypeResolver {

    /**
     * 用户类型请求头名称
     */
    @Value("${mortise.auth.user-type.header-name:" + HttpHeaderConstant.X_CLIENT_TYPE + "}")
    private String headerName;

    /**
     * 会员用户路径前缀列表
     */
    @Value("#{'${mortise.auth.user-type.member-paths:/api/app,/api/member,/api/v1/app}'.split(',')}")
    private List<String> memberPaths;

    /**
     * 默认用户类型
     */
    @Value("${mortise.auth.user-type.default:system}")
    private String defaultUserType;

    @Override
    public String resolve(HttpServletRequest request) {
        // 1. 优先从请求头获取用户类型
        String clientType = request.getHeader(headerName);
        if (StringUtils.hasText(clientType)) {
            String raw = clientType.toLowerCase().trim();

            // 兼容：如果上游直接传 userType（system/member），则直接返回
            if (UserType.SYSTEM.getCode().equals(raw) || UserType.MEMBER.getCode().equals(raw)) {
                log.debug("从请求头 {} 获取用户类型(userType): {}", headerName, raw);
                return raw;
            }

            // 统一：将 x-client-type 视为 clientType，再映射为 userType
            String normalizedClientType = ClientTypeConstant.normalize(raw);
            String userType = switch (normalizedClientType) {
                case ClientTypeConstant.SYSTEM -> UserType.SYSTEM.getCode();
                case ClientTypeConstant.APP, ClientTypeConstant.WEB, ClientTypeConstant.API -> UserType.MEMBER.getCode();
                default -> null;
            };

            if (userType != null) {
                log.debug("从请求头 {} 获取客户端类型(clientType): {} -> userType: {}", headerName, normalizedClientType, userType);
                return userType;
            }
        }

        // 2. 根据请求路径判断（兼容存在 context-path 的场景）
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri;
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        for (String memberPath : memberPaths) {
            if (path.startsWith(memberPath)) {
                log.debug("根据请求路径判断用户类型: {} (raw: {}) -> {}", path, requestUri, UserType.MEMBER.getCode());
                return UserType.MEMBER.getCode();
            }
        }

        // 3. 返回默认用户类型
        log.debug("使用默认用户类型: {} (raw: {}) -> {}", path, requestUri, defaultUserType);
        return defaultUserType;
    }

    @Override
    public int getOrder() {
        // 默认实现优先级较低，允许业务模块覆盖
        return 100;
    }
}
