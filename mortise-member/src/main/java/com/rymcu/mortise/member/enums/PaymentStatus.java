package com.rymcu.mortise.member.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author ronger
 */
@Getter
public enum PaymentStatus {
    /**
     * 未支付
     */
    UNPAID(0, "未支付"),
    /**
     * 部分支付
     */
    PARTIAL_PAID(1, "部分支付"),
    /**
     * 已支付
     */
    PAID(2, "已支付"),
    /**
     * 已退款
     */
    REFUNDED(3, "已退款"),
    /**
     * 支付失败
     */
    FAILED(4, "支付失败");

    private final Integer code;
    private final String description;

    PaymentStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的支付状态代码: " + code);
    }
}
