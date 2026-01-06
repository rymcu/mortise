package com.rymcu.mortise.log.resolver;

import com.rymcu.mortise.log.spi.ClientTypeResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 客户端类型解析器链
 * <p>
 * 收集所有 ClientTypeResolver 实现，按优先级顺序调用
 *
 * @author ronger
 */
@Slf4j
@Component
public class ClientTypeResolverChain {

    private final List<ClientTypeResolver> resolvers;

    @Autowired
    public ClientTypeResolverChain(Optional<List<ClientTypeResolver>> resolversOptional) {
        this.resolvers = resolversOptional.orElse(List.of());

        if (resolvers.isEmpty()) {
            log.warn("未发现任何 ClientTypeResolver 实现，将使用默认值");
        } else {
            log.info("发现 {} 个 ClientTypeResolver 实现", resolvers.size());
            resolvers.stream()
                    .sorted(Comparator.comparingInt(ClientTypeResolver::getOrder))
                    .forEach(r -> log.debug("  - {} (order={})", r.getClass().getSimpleName(), r.getOrder()));
        }
    }

    /**
     * 解析客户端类型
     *
     * @param request HTTP请求
     * @return 客户端类型
     */
    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return ClientTypeResolver.CLIENT_TYPE_UNKNOWN;
        }

        // 按优先级排序后遍历
        return resolvers.stream()
                .sorted(Comparator.comparingInt(ClientTypeResolver::getOrder))
                .filter(resolver -> resolver.supports(request))
                .findFirst()
                .map(resolver -> resolver.resolve(request))
                .orElse(ClientTypeResolver.CLIENT_TYPE_UNKNOWN);
    }
}
