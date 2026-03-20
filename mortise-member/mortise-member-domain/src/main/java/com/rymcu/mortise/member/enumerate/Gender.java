package com.rymcu.mortise.member.enumerate;

import lombok.Getter;

/**
 * 性别枚举
 * <p>
 * 同时映射微信 OAuth2 返回的 sex 数值：1=男, 2=女, 0=其他
 *
 * @author ronger
 */
@Getter
public enum Gender {

    MALE("male", 1, "男"),
    FEMALE("female", 2, "女"),
    OTHER("other", 0, "其他");

    /** 存储值（与数据库字段一致） */
    private final String value;
    /** 微信 sex 编码 */
    private final int wechatSexCode;
    private final String description;

    Gender(String value, int wechatSexCode, String description) {
        this.value = value;
        this.wechatSexCode = wechatSexCode;
        this.description = description;
    }

    /**
     * 根据微信 sex 编码获取性别
     */
    public static Gender fromWechatSexCode(int sexCode) {
        for (Gender gender : values()) {
            if (gender.wechatSexCode == sexCode) {
                return gender;
            }
        }
        return OTHER;
    }
}
