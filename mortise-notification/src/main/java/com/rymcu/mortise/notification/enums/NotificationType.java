package com.rymcu.mortise.notification.enums;

/**
 * 通知类型枚举
 *
 * @author ronger
 */
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
    PUSH("push", "推送通知");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
