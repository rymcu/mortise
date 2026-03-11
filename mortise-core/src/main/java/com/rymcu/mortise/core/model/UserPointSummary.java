package com.rymcu.mortise.core.model;

/**
 * 用户积分摘要
 *
 * @param userId      用户 ID
 * @param points      当前积分
 * @param memberLevel 当前等级标签
 */
public record UserPointSummary(
        Long userId,
        Integer points,
        String memberLevel
) {
}
