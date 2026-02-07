package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图标类型枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum IconType {

    /**
     * 图片
     */
    IMAGE("image", "图片"),

    /**
     * 字体图标
     */
    ICON("icon", "字体图标"),

    /**
     * Lottie动画
     */
    LOTTIE("lottie", "Lottie动画");

    private final String code;
    private final String description;

    public static IconType fromCode(String code) {
        for (IconType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
