package com.rymcu.mortise.agent.api.contract.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 会话信息响应对象。
 */
public record ConversationInfo(
        Long id,
        Long userId,
        String title,
        Integer status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedTime
) {
}
