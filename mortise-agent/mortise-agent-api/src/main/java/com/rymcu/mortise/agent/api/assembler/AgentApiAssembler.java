package com.rymcu.mortise.agent.api.assembler;

import com.rymcu.mortise.agent.api.contract.request.AgentChatRequest;
import com.rymcu.mortise.agent.api.contract.response.AgentChatResponse;
import com.rymcu.mortise.agent.api.contract.response.AgentModelInfo;
import com.rymcu.mortise.agent.api.contract.response.ConversationInfo;
import com.rymcu.mortise.agent.api.contract.response.TokenUsage;
import com.rymcu.mortise.agent.api.contract.response.ToolCallRecord;
import com.rymcu.mortise.agent.application.command.AgentChatCommand;
import com.rymcu.mortise.agent.application.result.AgentChatResult;
import com.rymcu.mortise.agent.application.result.AiModelResult;
import com.rymcu.mortise.agent.application.result.AiProviderResult;
import com.rymcu.mortise.agent.application.result.ConversationInfoResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentApiAssembler {

    public ConversationInfo toConversationInfo(ConversationInfoResult conversationInfo) {
        return new ConversationInfo(
                conversationInfo.id(),
                conversationInfo.userId(),
                conversationInfo.title(),
                conversationInfo.status(),
                conversationInfo.createdTime(),
                conversationInfo.updatedTime()
        );
    }

    public AgentModelInfo toModelInfo(AiProviderResult provider, List<AgentModelInfo.ModelItem> models) {
        return new AgentModelInfo(provider.code(), provider.name(), models);
    }

    public AgentModelInfo.ModelItem toModelItem(AiModelResult model) {
        return new AgentModelInfo.ModelItem(model.modelName(), model.displayName());
    }

    public AgentChatCommand toCommand(AgentChatRequest request, String conversationId) {
        List<AgentChatCommand.HistoryMessage> history = request.history() == null ? null : request.history().stream()
                .map(this::toHistoryMessage)
                .toList();
        return new AgentChatCommand(
                request.message(),
                conversationId,
                request.modelType(),
                request.modelName(),
                history,
                request.metadata()
        );
    }

    public AgentChatResponse toChatResponse(AgentChatResult result) {
        return new AgentChatResponse(
                result.conversationId(),
                result.content(),
                result.intent(),
                result.modelType(),
                result.modelName(),
                result.toolCalls() == null ? null : result.toolCalls().stream()
                        .map(toolCall -> new ToolCallRecord(
                                toolCall.callId(),
                                toolCall.toolName(),
                                toolCall.arguments(),
                                toolCall.result(),
                                toolCall.success(),
                                toolCall.errorMessage()
                        ))
                        .toList(),
                result.tokenUsage() == null ? null : new TokenUsage(
                        result.tokenUsage().promptTokens(),
                        result.tokenUsage().completionTokens(),
                        result.tokenUsage().totalTokens()
                ),
                result.metadata()
        );
    }

    private AgentChatCommand.HistoryMessage toHistoryMessage(AgentChatRequest.ChatHistoryItem item) {
        if (item == null) {
            return null;
        }
        AgentChatCommand.HistoryMessage.Role role = AgentChatCommand.HistoryMessage.Role.valueOf(item.role().name());
        return new AgentChatCommand.HistoryMessage(role, item.content());
    }
}
