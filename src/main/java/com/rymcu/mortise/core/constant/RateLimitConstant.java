package com.rymcu.mortise.core.constant;

/**
 * 限流相关常量
 *
 * @author ronger
 */
public final class RateLimitConstant {

    /**
     * 私有构造函数，防止实例化
     */
    private RateLimitConstant() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // === IP 相关常量 ===
    /**
     * 未知IP的默认值
     */
    public static final String UNKNOWN_IP = "unknown";

    // === 用户认证相关常量 ===
    /**
     * Spring Security 匿名用户标识
     */
    public static final String ANONYMOUS_USER_NAME = "anonymousUser";

    /**
     * 匿名用户ID的默认值
     */
    public static final String ANONYMOUS_USER_ID = "anonymous";

    // === SpEL 表达式相关常量 ===
    /**
     * SpEL 表达式执行失败时的默认返回值
     */
    public static final String SPEL_ERROR_RESULT = "spel_error";

    // === Redis Key 相关常量 ===
    /**
     * 限流Redis键的前缀
     */
    public static final String RATE_LIMIT_KEY_PREFIX = "resilience4j:rate_limit:";

    // === 日志消息常量 ===
    /**
     * 自定义键类型警告消息
     */
    public static final String CUSTOM_KEY_TYPE_WARNING_MESSAGE = "CUSTOM keyType 指定但 keyExpression 为空，使用默认 IP_AND_METHOD 策略";

    /**
     * 降级方法执行失败异常消息
     */
    public static final String FALLBACK_EXECUTION_FAILED_MESSAGE = "限流触发且降级方法执行失败";
}