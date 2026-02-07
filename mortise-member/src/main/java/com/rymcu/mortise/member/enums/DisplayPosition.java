package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 展示位置枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum DisplayPosition {

    /**
     * 首页
     */
    HOME("home", "首页"),

    /**
     * 分类页
     */
    CATEGORY("category", "分类页"),

    /**
     * 会员中心
     */
    MEMBER("member", "会员中心");

    private final String code;
    private final String description;

    public static DisplayPosition fromCode(String code) {
        for (DisplayPosition position : values()) {
            if (position.getCode().equals(code)) {
                return position;
            }
        }
        return null;
    }
}
