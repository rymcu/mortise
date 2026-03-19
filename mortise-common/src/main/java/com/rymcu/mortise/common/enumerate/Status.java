package com.rymcu.mortise.common.enumerate;

import lombok.Getter;

/**
 * 通用状态枚举（0=禁用, 1=启用）
 *
 * @author ronger
 */
@Getter
public enum Status {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 启用 */
    ENABLED(1, "启用");

    private final int code;
    private final String description;

    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
