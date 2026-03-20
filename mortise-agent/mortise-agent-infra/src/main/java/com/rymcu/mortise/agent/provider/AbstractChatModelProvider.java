package com.rymcu.mortise.agent.provider;

import com.rymcu.mortise.agent.model.*;
import com.rymcu.mortise.agent.spi.ChatModelProvider;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象聊天模型提供者
 * 提供通用的消息转换和响应处理逻辑
 */
public abstract class AbstractChatModelProvider implements ChatModelProvider {
    
    @Override
    public ChatResponse chat(List<ChatMessage> messages, String modelName) {
        return chatWithFunctions(messages, modelName, List.of());
    }
    
    @Override
    public ChatResponse chatWithFunctions(
            List<ChatMessage> messages, 
            String modelName, 
            List<FunctionCallback> functionCallbacks) {
        Prompt prompt = convertToPrompt(messages);
        org.springframework.ai.chat.model.ChatResponse response = callModel(prompt, resolveModelName(modelName), functionCallbacks);
        return convertResponse(response);
    }
    
    /**
     * 调用底层模型
     */
    protected abstract org.springframework.ai.chat.model.ChatResponse callModel(
            Prompt prompt, String modelName, List<FunctionCallback> functionCallbacks);
    
    /**
     * 获取 Spring AI 的 ChatModel
     */
    protected abstract org.springframework.ai.chat.model.ChatModel getChatModel();
    
    /**
     * 创建 ChatOptions，子类可覆盖
     */
    protected abstract ChatOptions createChatOptions(String modelName, List<FunctionCallback> functionCallbacks);
    
    protected Prompt convertToPrompt(List<ChatMessage> messages) {
        List<Message> springMessages = messages.stream()
            .map(this::convertMessage)
            .collect(Collectors.toList());
        return new Prompt(springMessages);
    }
    
    protected Message convertMessage(ChatMessage message) {
        return switch (message.role()) {
            case SYSTEM -> new SystemMessage(message.content());
            case USER -> new UserMessage(message.content());
            case ASSISTANT -> new AssistantMessage(message.content());
            case TOOL -> new UserMessage(message.content());
        };
    }
    
    protected ChatResponse convertResponse(
            org.springframework.ai.chat.model.ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return ChatResponse.builder()
                .content("")
                .modelType(getModelType())
                .build();
        }
        
        String content = response.getResult().getOutput().getContent();
        
        var metadata = response.getMetadata();
        TokenUsage tokenUsage = null;
        if (metadata != null && metadata.getUsage() != null) {
            var usage = metadata.getUsage();
            tokenUsage = TokenUsage.of(
                usage.getPromptTokens().longValue(),
                usage.getGenerationTokens().longValue()
            );
        }
        
        return ChatResponse.builder()
            .content(content)
            .modelType(getModelType())
            .modelName(metadata != null ? metadata.getModel() : null)
            .tokenUsage(tokenUsage)
            .build();
    }
    
    protected String resolveModelName(String modelName) {
        return modelName != null ? modelName : getDefaultModelName();
    }
}
