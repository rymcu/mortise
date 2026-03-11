package com.rymcu.mortise.core.model;

import java.time.LocalDateTime;

/**
 * 用户徽章条目
 *
 * @param code        徽章编码
 * @param name        徽章名称
 * @param icon        徽章图标
 * @param description 徽章描述
 * @param earnedTime  获得时间
 */
public record UserBadgeEntry(
        String code,
        String name,
        String icon,
        String description,
        LocalDateTime earnedTime
) {
}
