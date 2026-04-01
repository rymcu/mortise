package com.rymcu.mortise.agent.infra.provider;

import com.rymcu.mortise.agent.kernel.constant.AgentConstants;
import com.rymcu.mortise.agent.kernel.model.ModelType;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.List;

/**
 * Anthropic Claude 模型提供者
 */
public class AnthropicChatModelProvider extends AbstractChatModelProvider {
    
    private final AnthropicChatModel chatModel;
    private final String defaultModel;
    private List<String> availableModels;
    
    public AnthropicChatModelProvider(AnthropicChatModel chatModel, String defaultModel) {
        this.chatModel = chatModel;
        this.defaultModel = defaultModel != null ? defaultModel : AgentConstants.ANTHROPIC_DEFAULT_MODEL;
        this.availableModels = List.of();
    }
    
    @Override
    public ModelType getModelType() {
        return ModelType.ANTHROPIC;
    }
    
    @Override
    public boolean isAvailable() {
        return chatModel != null;
    }
    
    @Override
    protected org.springframework.ai.chat.model.ChatResponse callModel(
            Prompt prompt, String modelName, List<FunctionCallback> functionCallbacks) {
        ChatOptions options = createChatOptions(modelName, functionCallbacks);
        Prompt enhancedPrompt = new Prompt(prompt.getInstructions(), options);
        return chatModel.call(enhancedPrompt);
    }
    
    @Override
    protected ChatModel getChatModel() {
        return chatModel;
    }
    
    @Override
    protected ChatOptions createChatOptions(String modelName, List<FunctionCallback> functionCallbacks) {
        AnthropicChatOptions.Builder builder = AnthropicChatOptions.builder()
            .model(modelName);
        
        if (functionCallbacks != null && !functionCallbacks.isEmpty()) {
            builder.functionCallbacks(functionCallbacks);
        }
        
        return builder.build();
    }
    
    @Override
    public String getDefaultModelName() {
        return defaultModel;
    }
    
    @Override
    public List<String> getAvailableModels() {
        return availableModels;
    }

    public void setAvailableModels(List<String> availableModels) {
        this.availableModels = availableModels != null ? List.copyOf(availableModels) : List.of();
    }
}
