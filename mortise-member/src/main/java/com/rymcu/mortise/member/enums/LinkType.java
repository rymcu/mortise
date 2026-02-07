package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 链接类型枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum LinkType {

    /**
     * 无链接
     */
    NONE("none", "无链接"),

    /**
     * 内部链接
     */
    INTERNAL("internal", "内部链接"),

    /**
     * 外部链接
     */
    EXTERNAL("external", "外部链接"),

    /**
     * 商品
     */
    PRODUCT("product", "商品"),

    /**
     * 课程
     */
    COURSE("course", "课程"),

    /**
     * 分类
     */
    CATEGORY("category", "分类"),

    /**
     * 小程序
     */
    MINIAPP("miniapp", "小程序");

    private final String code;
    private final String description;

    public static LinkType fromCode(String code) {
        for (LinkType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
