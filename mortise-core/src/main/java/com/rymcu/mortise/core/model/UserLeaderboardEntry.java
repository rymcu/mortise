package com.rymcu.mortise.core.model;

/**
 * 用户积分排行榜快照
 *
 * @param userId      用户 ID
 * @param nickname    昵称
 * @param avatarUrl   头像
 * @param points      积分
 * @param memberLevel 等级标识
 */
public record UserLeaderboardEntry(
        Long userId,
        String nickname,
        String avatarUrl,
        Integer points,
        String memberLevel
) {
}
