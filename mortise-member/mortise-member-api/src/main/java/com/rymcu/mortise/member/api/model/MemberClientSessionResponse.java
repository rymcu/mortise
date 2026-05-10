package com.rymcu.mortise.member.api.model;

import java.time.LocalDateTime;

/**
 * 会员客户端会话响应。
 *
 * @param id 会话ID
 * @param clientId 客户端ID
 * @param clientName 客户端名称
 * @param deviceName 设备名称
 * @param status 状态
 * @param lastActiveAt 最近活跃时间
 * @param revokedAt 撤销时间
 * @param createdTime 创建时间
 * @param current 是否当前会话
 */
public record MemberClientSessionResponse(
        Long id,
        String clientId,
        String clientName,
        String deviceName,
        String status,
        LocalDateTime lastActiveAt,
        LocalDateTime revokedAt,
        LocalDateTime createdTime,
        Boolean current
) {
}
