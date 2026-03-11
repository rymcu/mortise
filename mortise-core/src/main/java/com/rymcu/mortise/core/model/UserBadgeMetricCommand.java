package com.rymcu.mortise.core.model;

import java.util.Map;

/**
 * 用户徽章指标命令
 *
 * @param userId     用户 ID
 * @param sourceType 来源类型
 * @param sourceId   来源 ID
 * @param metrics    指标集合，key 对应 badge.conditionType
 */
public record UserBadgeMetricCommand(
        Long userId,
        String sourceType,
        Long sourceId,
        Map<String, Long> metrics
) {
}
