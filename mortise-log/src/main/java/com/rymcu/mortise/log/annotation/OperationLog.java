package com.rymcu.mortise.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 *
 * @author ronger
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operation() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回结果
     */
    boolean recordResult() default false;

    /**
     * 是否异步记录（推荐异步）
     */
    boolean async() default true;
}
