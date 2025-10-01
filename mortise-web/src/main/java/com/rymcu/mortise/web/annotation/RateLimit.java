package com.rymcu.mortise.web.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 用于标记需要限流的方法，集成 Resilience4j 功能
 *
 * @author ronger
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流器名称
     */
    String name() default "default";

    /**
     * 限流失败时的错误消息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 周期内允许的请求数
     */
    int limitForPeriod() default 10;

    /**
     * 刷新周期（秒）
     */
    long refreshPeriodSeconds() default 1;

    /**
     * 超时时间（毫秒）
     * 0表示不等待，立即返回
     */
    long timeoutMillis() default 100;

    /**
     * 限流key的生成策略
     */
    KeyType keyType() default KeyType.IP_AND_METHOD;

    /**
     * 自定义限流key表达式（SpEL）
     * 当keyType为CUSTOM时使用
     */
    String keyExpression() default "";

    /**
     * 限流失败时的错误代码
     */
    int errorCode() default 429;

    /**
     * 是否启用降级处理
     * 当启用时，限流触发后会调用降级方法而不是抛出异常
     */
    boolean enableFallback() default false;

    /**
     * 降级方法名
     * 必须在同一个类中，且方法签名相同
     */
    String fallbackMethod() default "";

    /**
     * Key生成类型枚举
     */
    enum KeyType {
        /**
         * 基于IP地址
         */
        IP,

        /**
         * 基于方法名
         */
        METHOD,

        /**
         * 基于IP地址和方法名
         */
        IP_AND_METHOD,

        /**
         * 基于用户ID（需要登录）
         */
        USER_ID,

        /**
         * 基于IP地址和用户ID
         */
        IP_AND_USER_ID,

        /**
         * 自定义表达式
         */
        CUSTOM
    }
}
