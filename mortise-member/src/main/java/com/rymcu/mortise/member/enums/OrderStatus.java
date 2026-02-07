package com.rymcu.mortise.member.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author ronger
 */
@Getter
public enum OrderStatus {
    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),
    /**
     * 已支付
     */
    PAID(1, "已支付"),
    /**
     * 处理中
     */
    PROCESSING(2, "处理中"),
    /**
     * 已发货
     */
    SHIPPED(3, "已发货"),
    /**
     * 已送达
     */
    DELIVERED(4, "已送达"),
    /**
     * 已完成
     */
    COMPLETED(5, "已完成"),
    /**
     * 已取消
     */
    CANCELLED(6, "已取消"),
    /**
     * 已退款
     */
    REFUNDED(7, "已退款");

    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态代码: " + code);
    }
}
