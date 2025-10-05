package com.rymcu.mortise.auth.spi;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * OAuth2 登录成功处理器提供者 SPI
 * <p>
 * 允许各个模块（system、member）动态注册自己的 OAuth2 登录处理器
 * <p>
 * 实现模块需要:
 * 1. 实现此接口
 * 2. 使用 @Component 注解注册为 Spring Bean
 * 3. 指定支持的 registrationId 列表
 *
 * @author ronger
 * @since 1.0.0
 */
public interface OAuth2LoginSuccessHandlerProvider {

    /**
     * 获取处理器
     *
     * @return OAuth2 登录成功处理器
     */
    AuthenticationSuccessHandler getHandler();

    /**
     * 获取支持的 registrationId 列表
     * <p>
     * 示例:
     * - System 模块: ["logto", "logto-admin"]
     * - Member 模块: ["logto-member", "github", "google", "wechat"]
     *
     * @return 支持的 registrationId 数组
     */
    String[] getSupportedRegistrationIds();

    /**
     * 是否为默认处理器
     * <p>
     * 当找不到匹配的 registrationId 时，使用默认处理器
     * <p>
     * 建议只有一个 Provider 返回 true（通常是 System 模块）
     *
     * @return true 表示是默认处理器
     */
    default boolean isDefault() {
        return false;
    }

    /**
     * 优先级（数字越大优先级越高）
     * <p>
     * 当多个 Provider 声明支持同一个 registrationId 时，使用优先级高的
     * <p>
     * 默认优先级: 0
     *
     * @return 优先级值
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否启用
     * <p>
     * 可以根据配置动态启用/禁用
     *
     * @return true 表示启用
     */
    default boolean isEnabled() {
        return true;
    }
}
