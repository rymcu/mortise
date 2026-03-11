package com.rymcu.mortise.core.model;

/**
 * 用户徽章发放命令
 *
 * @param userId     用户 ID
 * @param badgeCode  徽章编码
 * @param sourceType 来源类型
 * @param sourceId   来源 ID
 */
public record UserBadgeAwardCommand(
        Long userId,
        String badgeCode,
        String sourceType,
        Long sourceId
) {
}
