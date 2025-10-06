package com.rymcu.mortise.common.enumerate;

/**
 * Created on 2025/10/6 23:08.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.common.enumerate
 */
public enum EnabledFlag {
    NO,
    YES;
    /**
     * 获取枚举对应的整数值
     * @return 0-非默认，1-默认
     */
    public int getValue() {
        return this.ordinal();
    }

    /**
     * 根据整数值获取枚举
     * @param value 整数值
     * @return 对应的枚举，null表示未找到
     */
    public static EnabledFlag valueOf(int value) {
        for (EnabledFlag flag : EnabledFlag.values()) {
            if (flag.ordinal() == value) {
                return flag;
            }
        }
        return null;
    }

    /**
     * 判断是否为启用
     * @return true-是启用，false-是禁用
     */
    public boolean isDefault() {
        return this == YES;
    }
}
