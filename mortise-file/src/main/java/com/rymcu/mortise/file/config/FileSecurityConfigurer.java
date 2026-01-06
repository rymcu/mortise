package com.rymcu.mortise.file.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * 文件模块的安全配置扩展
 * <p>
 * 通过 SecurityConfigurer SPI 配置 Auth 端点的访问权限
 * <strong>无需依赖 mortise-auth 模块</strong>（仅依赖 SPI 接口）
 * <p>
 * <strong>架构设计原则</strong>:
 * - file 和 auth 是同一层级的模块，不应该有依赖关系
 * - 使用 SPI 扩展机制实现解耦
 * - 文件模块独立管理自己的安全策略
 *
 * @author ronger
 */
@Slf4j
@Component
public class FileSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 200; // 低优先级
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        // 文件访问接口 - 无需认证
        registry.requestMatchers("/files/**").permitAll();

        log.info("文件模块安全配置已加载: /files/** 端点放行");
    }

    @Override
    public boolean isEnabled() {
        // 文件端点配置始终启用
        return true;
    }
}
