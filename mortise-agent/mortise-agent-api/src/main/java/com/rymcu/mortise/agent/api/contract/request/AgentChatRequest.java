package com.rymcu.mortise.agent.api.contract.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * Agent 聊天请求。
 */
public record AgentChatRequest(
        @NotBlank(message = "消息内容不能为空")
        String message,
        String conversationId,
        String modelType,
        String modelName,
        List<ChatHistoryItem> history,
        Map<String, Object> metadata
) {

    public record ChatHistoryItem(
            Role role,
            String content
    ) {
        public enum Role {
            SYSTEM,
            USER,
            ASSISTANT,
            TOOL
        }
    }
}
