package com.rymcu.mortise.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created on 2026/2/4 8:50.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.enums
 */
@Getter
@AllArgsConstructor
public enum TargetType {
    /**
     * 课程
     */
    COURSE("course", "课程"),
    COURSE_SCHEDULE("course_schedule", "课程排期"),
    BUNDLE("bundle", "套餐"),
    MEMBERSHIP("membership", "会员"),
    LIVE_EVENT("live_event", "直播活动"),
    MATERIAL("material", "资料"),
    SERVICE("service", "服务"),
    ;

    private final String code;
    private final String description;

    public static TargetType fromCode(String code) {
        for (TargetType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
