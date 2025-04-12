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
@Retention(RetentionPolicy.RUNTIME) // 必须是 RUNTIME，AOP 才能在运行时获取到
@Target(ElementType.FIELD)       // 作用于字段上
public @interface Dict {
    /**
     * 字典编码 (对应 sys_dict_type 表中的 dict_code)
     */
    String value() default "";

    /**
     * 如果是翻译数据库表字段，指定表名
     * (可选功能，用于更复杂的场景，例如: dictCode="user", dictTable="sys_user", dictField="user_name", dictValueField="user_id")
     */
     String dictTable() default "";

    /**
     * 如果是翻译数据库表字段，指定文本字段名
     */
     String dictTextField() default "";

    /**
     * 如果是翻译数据库表字段，指定值字段名
     */
     String dictValueField() default "";
}
