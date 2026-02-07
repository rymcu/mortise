package com.rymcu.mortise.member.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 *
 * @author ronger
 */
@Getter
public enum PaymentMethod {
    /**
     * 支付宝
     */
    ALIPAY("alipay", "支付宝"),
    /**
     * 微信支付
     */
    WECHAT("wechat", "微信支付"),
    /**
     * 银联
     */
    UNIONPAY("unionpay", "银联"),
    /**
     * 余额支付
     */
    BALANCE("balance", "余额"),
    /**
     * 积分支付
     */
    POINTS("points", "积分"),
    /**
     * 银行转账
     */
    BANK_TRANSFER("bank_transfer", "银行转账"),
    /**
     * PayPal
     */
    PAYPAL("paypal", "PayPal");

    private final String code;
    private final String description;

    PaymentMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentMethod fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知的支付方式代码: " + code);
    }
}
