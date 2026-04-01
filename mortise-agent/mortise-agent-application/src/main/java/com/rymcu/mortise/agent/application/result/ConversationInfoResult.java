package com.rymcu.mortise.agent.application.result;

import java.time.LocalDateTime;

/**
 * 会话信息结果。
 */
public record ConversationInfoResult(
        Long id,
        Long userId,
        String title,
        Integer status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
