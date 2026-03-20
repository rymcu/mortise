package com.rymcu.mortise.agent.router;

import com.rymcu.mortise.agent.model.AgentIntent;
import com.rymcu.mortise.agent.model.ChatMessage;
import com.rymcu.mortise.agent.model.ChatResponse;
import com.rymcu.mortise.agent.model.IntentResult;
import com.rymcu.mortise.agent.provider.ChatModelProviderRegistry;
import com.rymcu.mortise.agent.spi.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 语义路由器
 * 根据意图识别结果将请求路由到不同的处理器
 * 使用 Spring AI 的 FunctionCallback
 */
public class SemanticRouter {
    
    private static final Logger log = LoggerFactory.getLogger(SemanticRouter.class);
    
    private final IntentClassifier intentClassifier;
    private final ChatModelProviderRegistry providerRegistry;
    private final List<FunctionCallback> functionCallbacks;
    private final ChatHandler chatHandler;
    private final ToolCallHandler toolCallHandler;
    
    public SemanticRouter(
            IntentClassifier intentClassifier,
            ChatModelProviderRegistry providerRegistry,
            List<FunctionCallback> functionCallbacks,
            ChatHandler chatHandler,
            ToolCallHandler toolCallHandler
    ) {
        this.intentClassifier = intentClassifier;
        this.providerRegistry = providerRegistry;
        this.functionCallbacks = functionCallbacks;
        this.chatHandler = chatHandler;
        this.toolCallHandler = toolCallHandler;
    }
    
    /**
     * 路由请求
     */
    public ChatResponse route(List<ChatMessage> messages, String modelName) {
        String userMessage = extractLastUserMessage(messages);
        List<String> availableFunctions = getFunctionNames();
        
        IntentResult intent = intentClassifier.classify(userMessage, availableFunctions);
        log.debug("Intent classified: {} with confidence {}", intent.intent(), intent.confidence());
        
        return switch (intent.intent()) {
            case CHAT -> chatHandler.handle(messages, modelName);
            case TOOL_CALL -> {
                List<FunctionCallback> recommendedCallbacks = filterCallbacks(intent.recommendedTools());
                yield toolCallHandler.handle(messages, modelName, recommendedCallbacks);
            }
        };
    }
    
    private List<String> getFunctionNames() {
        return functionCallbacks.stream()
            .map(FunctionCallback::getName)
            .collect(Collectors.toList());
    }
    
    private List<FunctionCallback> filterCallbacks(List<String> names) {
        if (names == null || names.isEmpty()) {
            return functionCallbacks;
        }
        return functionCallbacks.stream()
            .filter(fc -> names.contains(fc.getName()))
            .collect(Collectors.toList());
    }
    
    private String extractLastUserMessage(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.role() == ChatMessage.Role.USER) {
                return msg.content();
            }
        }
        return "";
    }
    
    /**
     * 聊天处理器接口
     */
    @FunctionalInterface
    public interface ChatHandler {
        ChatResponse handle(List<ChatMessage> messages, String modelName);
    }
    
    /**
     * 工具调用处理器接口
     */
    @FunctionalInterface
    public interface ToolCallHandler {
        ChatResponse handle(List<ChatMessage> messages, String modelName, List<FunctionCallback> callbacks);
    }
}