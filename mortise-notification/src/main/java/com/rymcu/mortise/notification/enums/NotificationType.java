package com.rymcu.mortise.notification.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 通知类型枚举
 *
 * @author ronger
 */
@Getter
public enum NotificationType {

    /**
     * 邮件通知
     */
    EMAIL("email", "邮件通知"),

    /**
     * 短信通知
     */
    SMS("sms", "短信通知"),

    /**
     * 站内消息
     */
    SYSTEM("system", "站内消息"),

    /**
     * 推送通知
     */
    PUSH("push", "推送通知"),

    /**
     * 微信通知
     */
    WECHAT("wechat", "微信通知");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 查找通知类型
     *
     * @param code 类型代码（如 "email"、"sms"）
     * @return 对应的枚举值
     */
    public static Optional<NotificationType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equalsIgnoreCase(code))
                .findFirst();
    }

}
