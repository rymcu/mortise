package com.rymcu.mortise.auth.util;

/**
 * OAuth2 提供商工具类
 *
 * <p>提供跨组件共享的 OAuth2 提供商判断逻辑和常量，避免重复实现</p>
 *
 * @author ronger
 * @since 1.0.0
 */
public class OAuth2ProviderUtils {

    // ==================== OAuth2 Provider 常量 ====================

    /**
     * 微信开放平台（网站应用、APP）
     */
    public static final String PROVIDER_WECHAT = "wechat";

    /**
     * 微信公众号（公众号网页授权）
     */
    public static final String PROVIDER_WECHAT_MP = "wechat_mp";

    /**
     * 微信小程序
     */
    public static final String PROVIDER_WECHAT_MINIAPP = "wechat_miniapp";

    /**
     * GitHub
     */
    public static final String PROVIDER_GITHUB = "github";

    /**
     * Google
     */
    public static final String PROVIDER_GOOGLE = "google";

    /**
     * Logto
     */
    public static final String PROVIDER_LOGTO = "logto";

    /**
     * 未知提供商
     */
    public static final String PROVIDER_UNKNOWN = "unknown";

    // ==================== 判断方法 ====================

    /**
     * 判断是否是微信相关的 OAuth2 提供商
     *
     * <p>支持多种微信相关的命名模式：wechat、weixin、wx 前缀</p>
     *
     * @param registrationId 客户端注册ID
     * @return 是否是微信提供商
     */
    public static boolean isWeChatProvider(String registrationId) {
        if (registrationId == null) {
            return false;
        }
        String lower = registrationId.toLowerCase();
        return lower.contains("wechat") || lower.contains("weixin") || lower.startsWith("wx");
    }

    /**
     * 根据 registrationId 确定标准化的提供商类型
     *
     * <p>
     * 将各种命名风格的 registrationId 映射为标准化的提供商类型：
     * <ul>
     *   <li>wechat* → wechat</li>
     *   <li>github* → github</li>
     *   <li>google* → google</li>
     *   <li>logto* → logto</li>
     *   <li>其他 → 原值或 unknown</li>
     * </ul>
     * </p>
     *
     * @param registrationId OAuth2 客户端注册ID
     * @return 标准化的提供商类型
     */
    public static String determineProviderType(String registrationId) {
        if (registrationId == null) {
            return PROVIDER_UNKNOWN;
        }

        String lower = registrationId.toLowerCase();

        if (lower.startsWith("wechat") || lower.startsWith("weixin") || lower.startsWith("wx")) {
            return PROVIDER_WECHAT;
        } else if (lower.startsWith("github")) {
            return PROVIDER_GITHUB;
        } else if (lower.startsWith("google")) {
            return PROVIDER_GOOGLE;
        } else if (lower.startsWith("logto")) {
            return PROVIDER_LOGTO;
        }

        return registrationId;
    }

    /**
     * 判断提供商类型是否为微信
     *
     * @param providerType 提供商类型
     * @return 是否是微信
     */
    public static boolean isWeChatProviderType(String providerType) {
        if (providerType == null) {
            return false;
        }
        return PROVIDER_WECHAT.equalsIgnoreCase(providerType)
                || PROVIDER_WECHAT_MP.equalsIgnoreCase(providerType)
                || PROVIDER_WECHAT_MINIAPP.equalsIgnoreCase(providerType);
    }

    private OAuth2ProviderUtils() {
        throw new AssertionError("工具类不应该被实例化");
    }
}
