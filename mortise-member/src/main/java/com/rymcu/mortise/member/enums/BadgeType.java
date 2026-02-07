package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角标类型枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum BadgeType {

    /**
     * 热门
     */
    HOT("hot", "热门"),

    /**
     * 新品
     */
    NEW("new", "新品"),

    /**
     * 促销
     */
    SALE("sale", "促销"),

    /**
     * 自定义
     */
    CUSTOM("custom", "自定义");

    private final String code;
    private final String description;

    public static BadgeType fromCode(String code) {
        for (BadgeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
