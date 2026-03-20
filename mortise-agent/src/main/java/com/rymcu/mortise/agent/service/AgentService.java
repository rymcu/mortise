package com.rymcu.mortise.agent.service;

import com.rymcu.mortise.agent.agent.ReActAgent;
import com.rymcu.mortise.agent.agent.ReActAgentFactory;
import com.rymcu.mortise.agent.config.AgentProperties;
import com.rymcu.mortise.agent.model.*;
import com.rymcu.mortise.agent.prompt.AgentPromptTemplate;
import com.rymcu.mortise.agent.provider.ChatModelProviderRegistry;
import com.rymcu.mortise.agent.router.FunctionCallingIntentClassifier;
import com.rymcu.mortise.agent.router.LlmIntentClassifier;
import com.rymcu.mortise.agent.router.RuleBasedIntentClassifier;
import com.rymcu.mortise.agent.router.SemanticRouter;
import com.rymcu.mortise.agent.spi.ChatModelProvider;
import com.rymcu.mortise.agent.spi.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 服务门面
 * 提供统一的 Agent 调用入口
 * 使用 Spring AI 原生的 FunctionCallback 机制
 */
@Service
public class AgentService {
    
    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    
    private final ChatModelProviderRegistry providerRegistry;
    private final List<FunctionCallback> functionCallbacks;
    private final ReActAgentFactory agentFactory;
    private final AgentProperties properties;
    private final AgentPromptTemplate promptTemplate;
    private volatile SemanticRouter router;
    
    public AgentService(
            ChatModelProviderRegistry providerRegistry,
            List<FunctionCallback> functionCallbacks,
            ReActAgentFactory agentFactory,
            AgentProperties properties,
            AgentPromptTemplate promptTemplate
    ) {
        this.providerRegistry = providerRegistry;
        this.functionCallbacks = functionCallbacks != null ? functionCallbacks : List.of();
        this.agentFactory = agentFactory;
        this.properties = properties;
        this.promptTemplate = promptTemplate;
    }
    
    /**
     * 延迟初始化 Router，确保 provider 已注册
     */
    private SemanticRouter getRouter() {
        if (router == null) {
            synchronized (this) {
                if (router == null) {
                    router = buildRouter();
                }
            }
        }
        return router;
    }
    
    /**
     * 处理聊天请求
     */
    public ChatResponse chat(ChatRequest request) {
        log.debug("Processing chat request: {}", request.message());
        
        // 获取模型提供者
        ChatModelProvider provider = getProvider(request.modelType());
        String modelName = resolveModelName(request, provider);
        
        // 构建消息列表
        List<ChatMessage> messages = buildMessages(request);
        
        // 通过语义路由处理
        return getRouter().route(messages, modelName);
    }
    
    /**
     * 简单聊天（无工具调用）
     */
    public ChatResponse simpleChat(String message) {
        return simpleChat(message, null, null);
    }
    
    /**
     * 简单聊天（指定模型）
     */
    public ChatResponse simpleChat(String message, ModelType modelType, String modelName) {
        ChatModelProvider provider = getProvider(modelType);
        List<ChatMessage> messages = List.of(ChatMessage.user(message));
        return provider.chat(messages, modelName != null ? modelName : provider.getDefaultModelName());
    }
    
    /**
     * 执行 ReAct Agent（使用所有已注册的 FunctionCallback）
     */
    public ChatResponse executeReAct(ChatRequest request) {
        return executeReAct(request, functionCallbacks);
    }
    
    /**
     * 执行 ReAct Agent（指定 FunctionCallback）
     */
    public ChatResponse executeReAct(ChatRequest request, List<FunctionCallback> callbacks) {
        ChatModelProvider provider = getProvider(request.modelType());
        String modelName = resolveModelName(request, provider);
        
        ReActAgent agent = agentFactory.create(provider);
        List<ChatMessage> messages = buildMessages(request);
        
        return agent.execute(messages, modelName, callbacks != null ? callbacks : List.of());
    }
    
    /**
     * 获取所有已注册的 FunctionCallback 名称
     */
    public List<String> getAvailableFunctionNames() {
        return functionCallbacks.stream()
            .map(FunctionCallback::getName)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private SemanticRouter buildRouter() {
        IntentClassifier classifier = createIntentClassifier();
        
        return new SemanticRouter(
            classifier,
            providerRegistry,
            functionCallbacks,
            this::handleChat,
            this::handleToolCall
        );
    }
    
    private IntentClassifier createIntentClassifier() {
        AgentProperties.RouterConfig routerConfig = properties.router();
        AgentProperties.IntentClassifierType classifierType = routerConfig.intentClassifierType();
        
        log.info("Creating intent classifier of type: {}", classifierType);
        
        return switch (classifierType) {
            case RULE_BASED -> new RuleBasedIntentClassifier();
            case FUNCTION_CALLING -> {
                // Function Calling 分类器使用默认模型提供者
                ChatModelProvider provider = providerRegistry.getDefaultProvider();
                yield new FunctionCallingIntentClassifier(provider);
            }
            case LLM_BASED -> {
                // LLM 分类器可以使用指定模型或默认模型
                ChatModelProvider provider;
                if (routerConfig.intentClassifierModel() != null) {
                    var modelConfig = routerConfig.intentClassifierModel();
                    provider = getProvider(modelConfig.type());
                } else {
                    provider = providerRegistry.getDefaultProvider();
                }
                yield new LlmIntentClassifier(provider, promptTemplate);
            }
        };
    }
    
    private ChatResponse handleChat(List<ChatMessage> messages, String modelName) {
        ChatModelProvider provider = providerRegistry.getDefaultProvider();
        return provider.chat(messages, modelName);
    }
    
    private ChatResponse handleToolCall(List<ChatMessage> messages, String modelName, List<FunctionCallback> callbacks) {
        ChatModelProvider provider = providerRegistry.getDefaultProvider();
        ReActAgent agent = agentFactory.create(provider);
        return agent.execute(messages, modelName, callbacks != null ? callbacks : functionCallbacks);
    }
    
    private ChatModelProvider getProvider(ModelType modelType) {
        if (modelType != null) {
            return providerRegistry.getRequiredProvider(modelType);
        }
        return providerRegistry.getDefaultProvider();
    }
    
    private String resolveModelName(ChatRequest request, ChatModelProvider provider) {
        if (request.modelName() != null) {
            return request.modelName();
        }
        if (request.modelType() != null) {
            return provider.getDefaultModelName();
        }
        var defaultModel = properties.defaultModel();
        return defaultModel.name() != null ? defaultModel.name() : provider.getDefaultModelName();
    }
    
    private List<ChatMessage> buildMessages(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        
        if (request.history() != null && !request.history().isEmpty()) {
            messages.addAll(request.history());
        }
        
        messages.add(ChatMessage.user(request.message()));
        return messages;
    }
}