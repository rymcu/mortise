package com.rymcu.mortise.agent.api.facade;

import com.rymcu.mortise.agent.api.contract.request.AgentChatRequest;
import com.rymcu.mortise.agent.api.contract.response.AgentChatResponse;
import com.rymcu.mortise.agent.api.contract.response.AgentModelInfo;
import com.rymcu.mortise.agent.api.contract.response.ConversationInfo;

import java.util.List;

/**
 * Agent API 门面。
 */
public interface AgentChatFacade {

    List<AgentModelInfo> listModels();

    List<ConversationInfo> listConversations(Long userId);

    void deleteConversation(Long conversationId, Long userId);

    AgentChatResponse chat(AgentChatRequest request, Long userId);

    AgentChatResponse chat(String message, String conversationId, String modelType, String modelName, Long userId);
}
