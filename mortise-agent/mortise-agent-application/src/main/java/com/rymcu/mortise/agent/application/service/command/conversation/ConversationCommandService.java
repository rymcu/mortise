package com.rymcu.mortise.agent.application.service.command.conversation;

import com.rymcu.mortise.agent.entity.Conversation;

/**
 * 会话命令服务。
 */
public interface ConversationCommandService {

    Conversation getOrCreate(String conversationId, Long userId, String firstMessage);

    Long getOrCreateConversationId(String conversationId, Long userId, String firstMessage);

    Conversation validateOwnership(Long conversationId, Long userId);

    void deleteConversation(Long conversationId, Long userId);
}
