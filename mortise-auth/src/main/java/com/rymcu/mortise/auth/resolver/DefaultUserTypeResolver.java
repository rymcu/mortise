package com.rymcu.mortise.auth.resolver;

import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.spi.UserTypeResolver;
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
    @Value("${mortise.auth.user-type.header-name:x-client-type}")
    private String headerName;

    /**
     * 会员用户路径前缀列表
     */
    @Value("${mortise.auth.user-type.member-paths:/api/app,/api/member,/api/v1/app}")
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
            String userType = clientType.toLowerCase().trim();
            log.debug("从请求头 {} 获取用户类型: {}", headerName, userType);
            return userType;
        }

        // 2. 根据请求路径判断
        String requestUri = request.getRequestURI();
        for (String memberPath : memberPaths) {
            if (requestUri.startsWith(memberPath)) {
                log.debug("根据请求路径判断用户类型: {} -> {}", requestUri, UserType.MEMBER.getCode());
                return UserType.MEMBER.getCode();
            }
        }

        // 3. 返回默认用户类型
        log.debug("使用默认用户类型: {} -> {}", requestUri, defaultUserType);
        return defaultUserType;
    }

    @Override
    public int getOrder() {
        // 默认实现优先级较低，允许业务模块覆盖
        return 100;
    }
}
