package com.rymcu.mortise.core.model;

/**
 * 会员能力快照
 *
 * @param userId      用户 ID
 * @param active      是否具备有效会员资格
 * @param memberLevel 会员等级
 */
public record MemberCapability(
        Long userId,
        boolean active,
        String memberLevel
) {
}