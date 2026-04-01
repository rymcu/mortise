package com.rymcu.mortise.agent.infra.provider;

import com.rymcu.mortise.agent.kernel.constant.AgentConstants;
import com.rymcu.mortise.agent.kernel.model.ModelType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

/**
 * OpenAI 模型提供者
 */
public class OpenAiChatModelProvider extends AbstractChatModelProvider {
    
    private final OpenAiChatModel chatModel;
    private final String defaultModel;
    private List<String> availableModels;
    
    public OpenAiChatModelProvider(OpenAiChatModel chatModel, String defaultModel) {
        this.chatModel = chatModel;
        this.defaultModel = defaultModel != null ? defaultModel : AgentConstants.OPENAI_DEFAULT_MODEL;
        this.availableModels = List.of();
    }
    
    @Override
    public ModelType getModelType() {
        return ModelType.OPENAI;
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
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder()
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
