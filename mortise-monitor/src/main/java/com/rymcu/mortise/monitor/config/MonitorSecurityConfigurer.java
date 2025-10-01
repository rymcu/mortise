package com.rymcu.mortise.monitor.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * 监控模块的安全配置扩展
 * <p>
 * 通过 SecurityConfigurer SPI 配置 Actuator 端点的访问权限
 * <strong>无需依赖 mortise-auth 模块</strong>（仅依赖 SPI 接口）
 * <p>
 * <strong>架构设计原则</strong>:
 * - monitor 和 auth 是同一层级的模块，不应该有依赖关系
 * - 使用 SPI 扩展机制实现解耦
 * - 监控模块独立管理自己的安全策略
 *
 * @author ronger
 */
@Slf4j
@Component
public class MonitorSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 50; // 较高优先级，确保监控端点配置优先生效
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        // Actuator 健康检查端点 - 无需认证
        registry.requestMatchers("/actuator/health").permitAll();
        registry.requestMatchers("/actuator/health/**").permitAll();
        // Actuator 信息端点 - 无需认证
        registry.requestMatchers("/actuator/info").permitAll();
        // Prometheus 指标端点 - 无需认证（生产环境建议限制访问）
        registry.requestMatchers("/actuator/prometheus").permitAll();
        // 限流器状态端点 - 无需认证
        registry.requestMatchers("/actuator/ratelimiters").permitAll();
        // 所有 Actuator 端点 - 无需认证（开发环境）
        registry.requestMatchers("/actuator/**").permitAll();

        log.info("监控模块安全配置已加载: Actuator 端点放行");
    }

    @Override
    public boolean isEnabled() {
        // 监控端点配置始终启用
        return true;
    }
}
