package com.rymcu.mortise.auth.enumerate;

/**
 * Created on 2025/10/9 23:43.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.enumerate
 */
public enum QrcodeState {
    WAITED,
    SCANNED,
    AUTHORIZED,
    CANCELED,
    EXPIRED;

    /**
     * 获取枚举对应的整数值
     * @return 0-等待扫码，1-已扫码，等待用户授权， 2-已扫码， 同意授权， 3-已扫码，用户取消授权， 4-已过期
     */
    public int getValue() {
        return this.ordinal();
    }

    /**
     * 根据整数值获取枚举
     * @param value 整数值
     * @return 对应的枚举，null表示未找到
     */
    public static QrcodeState valueOf(int value) {
        for (QrcodeState flag : QrcodeState.values()) {
            if (flag.ordinal() == value) {
                return flag;
            }
        }
        return null;
    }
}
