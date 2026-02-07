package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品类型枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum ProductType {

    /**
     * 课程
     */
    COURSE("course", "课程"),

    /**
     * 套餐
     */
    BUNDLE("bundle", "套餐"),

    /**
     * 会员
     */
    MEMBERSHIP("membership", "会员"),

    /**
     * 服务
     */
    SERVICE("service", "服务"),

    /**
     * 资料
     */
    MATERIAL("material", "资料"),

    /**
     * 直播活动
     */
    LIVE_EVENT("live_event", "直播活动");

    private final String code;
    private final String description;

    public static ProductType fromCode(String code) {
        for (ProductType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
