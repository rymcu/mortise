package com.rymcu.mortise.wechat.enumerate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信账号类型枚举
 *
 * @author ronger
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WeChatAccountType {

    /**
     * 微信公众号（订阅号、服务号）
     */
    MP("mp", "公众号", "微信公众平台账号，支持订阅号和服务号"),

    /**
     * 微信开放平台（网站应用）
     */
    OPEN("open", "开放平台", "微信开放平台账号，用于网站扫码登录"),

    /**
     * 微信小程序
     */
    MINI("mini", "小程序", "微信小程序账号"),

    /**
     * 企业微信
     */
    CP("cp", "企业微信", "企业微信账号");

    /**
     * 类型代码（数据库存储值）
     */
    @JsonValue
    private final String code;

    /**
     * 类型名称（用于显示）
     */
    private final String name;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据 code 获取枚举
     *
     * @param code 类型代码
     * @return 枚举实例，如果不存在返回 null
     */
    @JsonCreator
    public static WeChatAccountType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (WeChatAccountType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为公众号类型
     */
    public boolean isMp() {
        return this == MP;
    }

    /**
     * 判断是否为开放平台类型
     */
    public boolean isOpen() {
        return this == OPEN;
    }

    /**
     * 判断是否为小程序类型
     */
    public boolean isMini() {
        return this == MINI;
    }

    /**
     * 判断是否为企业微信类型
     */
    public boolean isCp() {
        return this == CP;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
