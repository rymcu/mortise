package com.rymcu.mortise.log.annotation;

import java.lang.annotation.*;

/**
 * API 日志注解
 * 用于记录 API 调用日志（请求、响应、耗时等）
 *
 * @author ronger
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {

    /**
     * API 描述
     */
    String value() default "";

    /**
     * 是否记录请求体
     */
    boolean recordRequestBody() default true;

    /**
     * 是否记录响应体
     */
    boolean recordResponseBody() default true;

    /**
     * 是否记录请求头
     */
    boolean recordHeaders() default false;
}
