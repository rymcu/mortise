package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存类型枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum InventoryType {

    /**
     * 无限库存
     */
    UNLIMITED("unlimited", "无限库存"),

    /**
     * 有限库存
     */
    LIMITED("limited", "有限库存"),

    /**
     * 预售
     */
    PREORDER("preorder", "预售");

    private final String code;
    private final String description;

    public static InventoryType fromCode(String code) {
        for (InventoryType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
