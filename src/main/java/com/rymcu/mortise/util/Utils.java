package com.rymcu.mortise.util;

import com.github.f4b6a3.ulid.UlidCreator;
import com.rymcu.mortise.core.constant.ProjectConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created on 2024/4/14 10:14.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.util
 */
public class Utils {

    public static final String UNKNOWN = "unknown";
    public static final int HASH_ITERATIONS = 1024;
    public static final int SALT_SIZE = 8;

    public static String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，那么取第一个ip为客户端ip
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 生成安全的密码
     *
     * @param plainPassword 密码
     * @return String 加密后的密码
     */
    public static String encryptPassword(String plainPassword) {
        return encryptJasyptPassword(plainPassword, System.getenv(ProjectConstant.ENCRYPTION_KEY));
    }

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

    public static String encryptJasyptPassword(String password, String jasyptPassword) {
        StringEncryptor encryptor = initPasswordEncryptor(jasyptPassword);
        return encryptor.encrypt(password);
    }


    public static void main(String[] args) {
        System.out.println(encryptJasyptPassword("XzHvhX4CDaN696oQAXdmlcsrqgWbkxRl", System.getenv(ProjectConstant.ENCRYPTION_KEY)));
    }

    public static int genCode() {
        return (int) ((Math.random() * 9 + 1) * 100000);
    }

    public static String genPassword() {
        return UlidCreator.getUlid().toString();
    }
}
