package com.rymcu.mortise.auth.resolver;

import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.spi.UserTypeResolver;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 用户类型解析器责任链
 * <p>
 * 管理所有 UserTypeResolver 实现，按优先级顺序调用，
 * 返回第一个非 null 的解析结果。
 * </p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTypeResolverChain {

    private final List<UserTypeResolver> resolvers;

    private List<UserTypeResolver> sortedResolvers;

    @PostConstruct
    public void init() {
        // 按优先级排序（数字越小优先级越高）
        sortedResolvers = resolvers.stream()
                .filter(UserTypeResolver::isEnabled)
                .sorted(Comparator.comparingInt(UserTypeResolver::getOrder))
                .toList();

        log.info("用户类型解析器链初始化完成，共 {} 个解析器:", sortedResolvers.size());
        sortedResolvers.forEach(resolver ->
                log.info("  - {} (优先级: {})", resolver.getClass().getSimpleName(), resolver.getOrder())
        );
    }

    /**
     * 解析用户类型
     *
     * @param request HTTP 请求
     * @return 用户类型标识
     */
    public String resolve(HttpServletRequest request) {
        for (UserTypeResolver resolver : sortedResolvers) {
            try {
                String userType = resolver.resolve(request);
                if (userType != null) {
                    log.debug("用户类型解析成功: {} (解析器: {})",
                            userType, resolver.getClass().getSimpleName());
                    return userType;
                }
            } catch (Exception e) {
                log.warn("解析器 {} 执行失败: {}",
                        resolver.getClass().getSimpleName(), e.getMessage());
            }
        }

        // 如果所有解析器都返回 null，使用默认值
        log.debug("所有解析器都未能解析用户类型，使用默认值: {}", UserType.SYSTEM.getCode());
        return UserType.SYSTEM.getCode();
    }
}
