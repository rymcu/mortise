package com.rymcu.mortise.member.enumerate;

import lombok.Getter;

/**
 * 验证码类型枚举
 *
 * @author ronger
 */
@Getter
public enum VerificationCodeType {

    SMS("sms", "短信验证码"),
    EMAIL("email", "邮箱验证码");

    private final String value;
    private final String description;

    VerificationCodeType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值匹配枚举（忽略大小写）
     */
    public static VerificationCodeType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (VerificationCodeType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为指定类型
     */
    public boolean matches(String value) {
        return this.value.equalsIgnoreCase(value);
    }
}
