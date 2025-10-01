package com.rymcu.mortise.common.util;

import com.github.f4b6a3.ulid.UlidCreator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * 通用工具类
 *
 * @author ronger
 */
public class Utils {

    public static final String UNKNOWN = "unknown";
    public static final int HASH_ITERATIONS = 1024;
    public static final int SALT_SIZE = 8;

    /**
     * 生成验证码
     */
    public static int genCode() {
        return (int) ((Math.random() * 9 + 1) * 100000);
    }

    /**
     * 生成密码（使用ULID）
     */
    public static String genPassword() {
        return UlidCreator.getUlid().toString();
    }

    /**
     * 加密密码
     * 
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        
        // 生成随机盐
        String salt = RandomStringUtils.randomAlphanumeric(SALT_SIZE);
        
        // 多次哈希加密
        String encrypted = password;
        for (int i = 0; i < HASH_ITERATIONS; i++) {
            encrypted = DigestUtils.sha256Hex(encrypted + salt);
        }
        
        // 返回盐 + 密文
        return salt + encrypted;
    }

    /**
     * 私有构造函数，防止实例化
     */
    private Utils() {
        throw new AssertionError("工具类不应该被实例化");
    }
}
