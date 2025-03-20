package com.rymcu.mortise.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 2025/3/20 22:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.annotation
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictInterceptor {
    /**
     * 字典类型编码
     */
    String dictTypeCode();

    /**
     * 后缀，默认为Text
     */
    String suffix() default "Text";
}
