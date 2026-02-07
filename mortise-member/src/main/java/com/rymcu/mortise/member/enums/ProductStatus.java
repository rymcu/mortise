package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {

    /**
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 上架
     */
    ON_SALE(1, "上架"),

    /**
     * 下架
     */
    OFF_SALE(2, "下架"),

    /**
     * 停产
     */
    DISCONTINUED(3, "停产");

    private final Integer code;
    private final String description;

    public static ProductStatus fromCode(Integer code) {
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
