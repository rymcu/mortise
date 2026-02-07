package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SKU状态枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum SkuStatus {

    /**
     * 上架
     */
    ACTIVE("active", "上架"),

    /**
     * 下架
     */
    INACTIVE("inactive", "下架"),

    /**
     * 缺货
     */
    OUT_OF_STOCK("out_of_stock", "缺货"),

    /**
     * 停产
     */
    DISCONTINUED("discontinued", "停产");

    private final String code;
    private final String description;

    public static SkuStatus fromCode(String code) {
        for (SkuStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
