package com.rymcu.mortise.auth.spi;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户类型解析器接口 (SPI)
 * <p>
 * 用于根据请求解析用户类型，支持多用户表登录场景。
 * 业务模块可以实现此接口来自定义用户类型解析逻辑。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;Component
 * public class CustomUserTypeResolver implements UserTypeResolver {
 *     &#64;Override
 *     public String resolve(HttpServletRequest request) {
 *         // 自定义解析逻辑
 *         return "custom-user-type";
 *     }
 *
 *     &#64;Override
 *     public int getOrder() {
 *         return 50; // 优先级高于默认实现
 *     }
 * }
 * </pre>
 *
 * @author ronger
 * @since 1.0.0
 */
public interface UserTypeResolver {

    /**
     * 根据请求解析用户类型
     *
     * @param request HTTP 请求
     * @return 用户类型标识（如 "system", "member"），返回 null 表示无法解析，交给下一个解析器处理
     */
    String resolve(HttpServletRequest request);

    /**
     * 获取优先级，数字越小优先级越高
     * <p>
     * 默认优先级为 100，自定义实现可以返回更小的值以获得更高的优先级
     * </p>
     *
     * @return 优先级
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 是否启用该解析器
     *
     * @return true 表示启用，false 表示禁用
     */
    default boolean isEnabled() {
        return true;
    }
}
