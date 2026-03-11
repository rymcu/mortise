package com.rymcu.mortise.core.model;

import java.time.LocalDateTime;

/**
 * 用户积分历史条目
 *
 * @param id            历史记录 ID
 * @param changeAmount  变更积分
 * @param currentPoints 变更后积分
 * @param reason        变更原因
 * @param bizType       业务类型
 * @param bizId         业务 ID
 * @param memberLevel   当前等级标签
 * @param createdTime   发生时间
 */
public record UserPointHistoryEntry(
        Long id,
        Integer changeAmount,
        Integer currentPoints,
        String reason,
        String bizType,
        Long bizId,
        String memberLevel,
        LocalDateTime createdTime
) {
}
