package com.rymcu.mortise.core.model;

/**
 * 用户积分变更命令
 *
 * @param userId       用户 ID
 * @param changeAmount 变更积分
 * @param reason       变更原因
 * @param bizType      业务类型
 * @param bizKey       业务幂等键
 * @param bizId        业务 ID
 */
public record UserPointAwardCommand(
        Long userId,
        Integer changeAmount,
        String reason,
        String bizType,
        String bizKey,
        Long bizId
) {
}
