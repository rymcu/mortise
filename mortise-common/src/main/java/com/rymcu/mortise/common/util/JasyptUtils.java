package com.rymcu.mortise.common.util;

import com.rymcu.mortise.common.constant.ProjectConstant;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.iv.RandomIvGenerator;

/**
 * Jasypt 加密工具类
 * <p>
 * 提供基于 Jasypt 的字符串加密/解密功能
 *
 * @author ronger
 */
public class JasyptUtils {

    /**
     * 生成安全的密码（使用环境变量中的加密密钥）
     *
     * @param plainPassword 明文密码
     * @return String 加密后的密码
     */
    public static String encryptPassword(String plainPassword) {
        return encryptJasyptPassword(plainPassword, System.getenv(ProjectConstant.ENCRYPTION_KEY));
    }

    /**
     * 初始化 Jasypt 密码加密器
     *
     * @param password 加密密钥
     * @return StringEncryptor
     */
    public static StringEncryptor initPasswordEncryptor(String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        // encryptor's private key
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGenerator(new RandomIvGenerator());
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * 使用指定的密钥加密密码
     *
     * @param password       待加密的密码
     * @param jasyptPassword 加密密钥
     * @return String 加密后的字符串
     */
    public static String encryptJasyptPassword(String password, String jasyptPassword) {
        StringEncryptor encryptor = initPasswordEncryptor(jasyptPassword);
        return encryptor.encrypt(password);
    }

    /**
     * 使用指定的密钥解密密码
     *
     * @param encryptedPassword 加密的密码
     * @param jasyptPassword    解密密钥
     * @return String 解密后的字符串
     */
    public static String decryptJasyptPassword(String encryptedPassword, String jasyptPassword) {
        StringEncryptor encryptor = initPasswordEncryptor(jasyptPassword);
        return encryptor.decrypt(encryptedPassword);
    }

    /**
     * 私有构造函数，防止实例化
     */
    private JasyptUtils() {
        throw new AssertionError("工具类不应该被实例化");
    }
}
