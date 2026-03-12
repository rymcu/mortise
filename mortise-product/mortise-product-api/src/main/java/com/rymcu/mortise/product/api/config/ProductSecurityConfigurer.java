package com.rymcu.mortise.product.api.config;

import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * 产品模块安全配置
 * <p>
 * 通过 SPI 扩展机制向主安全配置添加产品相关的授权规则
 * <p>
 * 路径规则:
 * - /api/v1/products/** : 公开产品目录读接口
 * - /api/v1/product-categories/** : 公开产品分类读接口
 * - 其他产品接口：需要会员认证 (ROLE_MEMBER)
 *
 * @author ronger
 */
@Slf4j
@Component
public class ProductSecurityConfigurer implements SecurityConfigurer {

    /**
     * 配置授权规则
     * <p>
     * 优先级设置为 50，在默认规则 (100) 之前执行
     */
    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        log.info("应用产品模块安全配置...");

        registry
                // 产品目录公开读接口
                .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/product-categories", "/api/v1/product-categories/**").permitAll()

                // 其他产品接口需要会员角色
                .requestMatchers("/api/v1/products/**", "/api/v1/product-categories/**").hasRole("MEMBER");

        log.info("产品模块安全配置完成: GET /api/v1/products/**, GET /api/v1/product-categories/** (公开), 其他产品接口需要 ROLE_MEMBER");
    }

    /**
     * 设置优先级为 50
     * 确保产品规则在 WebSecurityConfig 的 anyRequest().authenticated() 之前应用
     */
    @Override
    public int getOrder() {
        return 50;
    }
}
