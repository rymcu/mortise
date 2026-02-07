package com.rymcu.mortise.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * 生成订单号
     * 格式: yyyyMMddHHmmss + UUID后8位
     *
     * @return 订单号
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + uuid;
    }

    /**
     * 生成带前缀的订单号
     * 格式: prefix + yyyyMMddHHmmss + UUID后8位
     *
     * @param prefix 前缀
     * @return 带前缀的订单号
     */
    public static String generateOrderNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + timestamp + uuid;
    }

    /**
     * 私有构造函数，防止实例化
     */
    private Utils() {
        throw new AssertionError("工具类不应该被实例化");
    }

    /**
     * 主方法：接收一个邮箱地址，提取并格式化其域名（移除了TLD）。
     * @param email 完整的邮箱地址，如 "aa@bb.cc.com"
     * @return 格式化后的字符串，如 "Bb Cc"
     */
    public static String formatEmailDomain(String email) {
        // 1. 健壮性检查
        if (email == null || email.indexOf('@') <= 0) { // 确保'@'存在且不是第一个字符
            return "";
        }

        // 2. 提取@之后完整的域名部分
        String fullDomain = email.substring(email.indexOf('@') + 1);

        // 3. 移除顶级域名(TLD)，获取需要处理的部分
        int lastDotIndex = fullDomain.lastIndexOf('.');
        String relevantDomainPart;

        // 如果有点号，并且不是域名的唯一部分（例如 "domain.com" 而不是 ".com"）
        if (lastDotIndex > 0) {
            relevantDomainPart = fullDomain.substring(0, lastDotIndex);
        } else if (lastDotIndex == -1) {
            // 处理没有点号的域名，如 "user@localhost"
            relevantDomainPart = fullDomain;
        } else {
            // 处理无效域名，如 "user@.com"
            return "";
        }

        // 4. 调用辅助方法进行最终的格式化
        return formatDomainParts(relevantDomainPart);
    }

    /**
     * 辅助方法：将点分隔的字符串格式化为首字母大写、空格分隔。
     * @param domainPart 点分隔的字符串，如 "bb.cc"
     * @return 格式化后的字符串，如 "Bb Cc"
     */
    private static String formatDomainParts(String domainPart) {
        if (domainPart == null || domainPart.trim().isEmpty()) {
            return "";
        }

        return Arrays.stream(domainPart.split("\\."))
                .filter(part -> !part.isEmpty())
                .map(Utils::capitalize)
                .collect(Collectors.joining(" "));
    }

    /**
     * 辅助方法：将单个单词首字母大写，其余小写。
     */
    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        return word.substring(0, 1).toUpperCase(Locale.ROOT)
                + word.substring(1).toLowerCase(Locale.ROOT);
    }
}
