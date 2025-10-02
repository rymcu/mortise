package com.rymcu.mortise.common.enumerate;

/**
 * 默认标记枚举
 * 用于标识是否为默认项（如默认角色、默认配置等）
 *
 * @author ronger
 */
public enum DefaultFlag {
    /**
     * 非默认
     */
    NO,
    /**
     * 默认
     */
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
    public static DefaultFlag valueOf(int value) {
        for (DefaultFlag flag : DefaultFlag.values()) {
            if (flag.ordinal() == value) {
                return flag;
            }
        }
        return null;
    }

    /**
     * 判断是否为默认
     * @return true-是默认，false-不是默认
     */
    public boolean isDefault() {
        return this == YES;
    }
}
