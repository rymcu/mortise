package com.rymcu.mortise.agent.api.facade.impl;

import com.rymcu.mortise.agent.api.assembler.AgentApiAssembler;
import com.rymcu.mortise.agent.api.contract.request.AgentChatRequest;
import com.rymcu.mortise.agent.api.contract.response.AgentChatResponse;
import com.rymcu.mortise.agent.api.contract.response.AgentModelInfo;
import com.rymcu.mortise.agent.api.contract.response.ConversationInfo;
import com.rymcu.mortise.agent.api.facade.AgentChatFacade;
import com.rymcu.mortise.agent.application.command.AgentChatCommand;
import com.rymcu.mortise.agent.application.result.AgentChatResult;
import com.rymcu.mortise.agent.application.service.command.conversation.ConversationCommandService;
import com.rymcu.mortise.agent.application.service.chat.AgentService;
import com.rymcu.mortise.agent.application.service.query.ai.AiModelQueryService;
import com.rymcu.mortise.agent.application.service.query.ai.AiProviderQueryService;
import com.rymcu.mortise.agent.application.service.query.conversation.ConversationQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent API 门面实现。
 */
@Service
public class AgentChatFacadeImpl implements AgentChatFacade {

    private final AgentService agentService;
    private final ConversationCommandService conversationCommandService;
    private final ConversationQueryService conversationQueryService;
    private final AiProviderQueryService aiProviderQueryService;
    private final AiModelQueryService aiModelQueryService;
    private final AgentApiAssembler assembler;

    public AgentChatFacadeImpl(
            AgentService agentService,
            ConversationCommandService conversationCommandService,
            ConversationQueryService conversationQueryService,
            AiProviderQueryService aiProviderQueryService,
            AiModelQueryService aiModelQueryService,
            AgentApiAssembler assembler
    ) {
        this.agentService = agentService;
        this.conversationCommandService = conversationCommandService;
        this.conversationQueryService = conversationQueryService;
        this.aiProviderQueryService = aiProviderQueryService;
        this.aiModelQueryService = aiModelQueryService;
        this.assembler = assembler;
    }

    @Override
    public List<AgentModelInfo> listModels() {
        return aiProviderQueryService.listEnabledProviders().stream().map(provider -> {
            List<AgentModelInfo.ModelItem> models = aiModelQueryService.listEnabledModelsByProviderId(provider.id()).stream()
                    .map(assembler::toModelItem)
                    .toList();
            return assembler.toModelInfo(provider, models);
        }).toList();
    }

    @Override
    public List<ConversationInfo> listConversations(Long userId) {
        return conversationQueryService.listUserConversationInfos(userId).stream()
                .map(assembler::toConversationInfo)
                .toList();
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        conversationCommandService.deleteConversation(conversationId, userId);
    }

    @Override
    public AgentChatResponse chat(AgentChatRequest request, Long userId) {
        Long conversationId = resolveConversationId(request.conversationId(), userId, request.message());
        AgentChatResult response = agentService.chat(assembler.toCommand(request, String.valueOf(conversationId)));
        return assembler.toChatResponse(response.withConversationId(String.valueOf(conversationId)));
    }

    @Override
    public AgentChatResponse chat(
            String message,
            String conversationId,
            String modelType,
            String modelName,
            Long userId
    ) {
        Long resolvedConversationId = resolveConversationId(conversationId, userId, message);
        AgentChatCommand command = new AgentChatCommand(
                message,
                String.valueOf(resolvedConversationId),
                modelType,
                modelName,
                null,
                null
        );
        AgentChatResult response = agentService.chat(command);
        return assembler.toChatResponse(response.withConversationId(String.valueOf(resolvedConversationId)));
    }

    private Long resolveConversationId(String conversationId, Long userId, String firstMessage) {
        return conversationCommandService.getOrCreateConversationId(conversationId, userId, firstMessage);
    }
}
