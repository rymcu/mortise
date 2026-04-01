package com.rymcu.mortise.agent.application.command;

import java.util.List;
import java.util.Map;

/**
 * Agent 聊天命令。
 */
public record AgentChatCommand(
        String message,
        String conversationId,
        String modelType,
        String modelName,
        List<HistoryMessage> history,
        Map<String, Object> metadata
) {

    public record HistoryMessage(
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
