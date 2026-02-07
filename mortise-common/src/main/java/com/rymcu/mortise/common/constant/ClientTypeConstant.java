package com.rymcu.mortise.common.constant;

import org.springframework.util.StringUtils;

/**
 * 客户端类型常量与归一化工具
 * <p>
 * 用于将各种网关/客户端上报的类型值统一映射为：system/app/web/api/unknown
 * </p>
 *
 * @author ronger
 */
public final class ClientTypeConstant {

    public static final String SYSTEM = "system";
    public static final String APP = "app";
    public static final String WEB = "web";
    public static final String API = "api";
    public static final String UNKNOWN = "unknown";

    private ClientTypeConstant() {
        throw new AssertionError("常量类不应该被实例化");
    }

    /**
     * 归一化客户端类型
     *
     * @param raw 原始值
     * @return system/app/web/api/unknown
     */
    public static String normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return UNKNOWN;
        }

        String type = raw.toLowerCase().trim();
        return switch (type) {
            case "system", "admin" -> SYSTEM;
            case "app", "mobile", "android", "ios" -> APP;
            case "web", "browser" -> WEB;
            case "api", "openapi" -> API;
            default -> UNKNOWN;
        };
    }
}
