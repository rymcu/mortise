package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 平台类型枚举
 *
 * @author ronger
 */
@Getter
@AllArgsConstructor
public enum PlatformType {

    /**
     * 全平台
     */
    ALL("all", "全平台"),

    /**
     * H5
     */
    H5("h5", "H5"),

    /**
     * 小程序
     */
    MINIAPP("miniapp", "小程序"),

    /**
     * APP
     */
    APP("app", "APP");

    private final String code;
    private final String description;

    public static PlatformType fromCode(String code) {
        for (PlatformType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
