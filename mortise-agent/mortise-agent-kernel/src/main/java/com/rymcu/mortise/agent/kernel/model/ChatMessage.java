package com.rymcu.mortise.agent.kernel.model;

/**
 * 聊天消息
 */
public record ChatMessage(
    Role role,
    String content
) {
    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT,
        TOOL
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(Role.SYSTEM, content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage(Role.USER, content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage(Role.ASSISTANT, content);
    }

    public static ChatMessage tool(String content) {
        return new ChatMessage(Role.TOOL, content);
    }
}
