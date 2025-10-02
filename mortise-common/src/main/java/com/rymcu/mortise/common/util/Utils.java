package com.rymcu.mortise.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * 通用工具类
 *
 * @author ronger
 */
public class Utils {

    /**
     * 生成验证码
     */
    public static int genCode() {
        return (int) ((Math.random() * 9 + 1) * 100000);
    }

    /**
     * 生成密钥（使用 ULID）
     */
    public static String genKey() {
        return UlidCreator.getUlid().toString();
    }

    /**
     * 私有构造函数，防止实例化
     */
    private Utils() {
        throw new AssertionError("工具类不应该被实例化");
    }
}
