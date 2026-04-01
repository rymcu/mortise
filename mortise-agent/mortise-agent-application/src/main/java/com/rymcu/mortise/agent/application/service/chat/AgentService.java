package com.rymcu.mortise.agent.application.service.chat;

import com.rymcu.mortise.agent.application.command.AgentChatCommand;
import com.rymcu.mortise.agent.application.orchestration.ReActAgent;
import com.rymcu.mortise.agent.application.orchestration.ReActAgentFactory;
import com.rymcu.mortise.agent.application.policy.intent.FunctionCallingIntentClassifier;
import com.rymcu.mortise.agent.application.policy.intent.LlmIntentClassifier;
import com.rymcu.mortise.agent.application.policy.intent.RuleBasedIntentClassifier;
import com.rymcu.mortise.agent.application.policy.intent.SemanticRouter;
import com.rymcu.mortise.agent.application.prompt.AgentPromptTemplate;
import com.rymcu.mortise.agent.application.result.AgentChatResult;
import com.rymcu.mortise.agent.kernel.config.AgentProperties;
import com.rymcu.mortise.agent.kernel.model.ChatMessage;
import com.rymcu.mortise.agent.kernel.model.ChatResponse;
import com.rymcu.mortise.agent.kernel.model.ModelType;
import com.rymcu.mortise.agent.kernel.provider.ChatModelProviderRegistry;
import com.rymcu.mortise.agent.kernel.spi.ChatModelProvider;
import com.rymcu.mortise.agent.kernel.spi.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public AgentChatResult chat(AgentChatCommand command) {
        log.debug("Processing chat request: {}", command.message());

        ModelType requestedModelType = resolveModelType(command.modelType());
        ChatModelProvider provider = getProvider(requestedModelType);
        String modelName = resolveModelName(command, provider);

        List<ChatMessage> messages = buildMessages(command);

        return AgentChatResult.from(getRouter().route(messages, modelName));
    }
    
    /**
     * 简单聊天（无工具调用）
     */
    public AgentChatResult simpleChat(String message) {
        return simpleChat(message, null, null);
    }
    
    /**
     * 简单聊天（指定模型）
     */
    public AgentChatResult simpleChat(String message, ModelType modelType, String modelName) {
        ChatModelProvider provider = getProvider(modelType);
        List<ChatMessage> messages = List.of(ChatMessage.user(message));
        return AgentChatResult.from(provider.chat(messages, modelName != null ? modelName : provider.getDefaultModelName()));
    }
    
    /**
     * 执行 ReAct Agent（使用所有已注册的 FunctionCallback）
     */
    public AgentChatResult executeReAct(AgentChatCommand command) {
        return executeReAct(command, functionCallbacks);
    }
    
    /**
     * 执行 ReAct Agent（指定 FunctionCallback）
     */
    public AgentChatResult executeReAct(AgentChatCommand command, List<FunctionCallback> callbacks) {
        ModelType requestedModelType = resolveModelType(command.modelType());
        ChatModelProvider provider = getProvider(requestedModelType);
        String modelName = resolveModelName(command, provider);

        ReActAgent agent = agentFactory.create(provider);
        List<ChatMessage> messages = buildMessages(command);

        return AgentChatResult.from(agent.execute(messages, modelName, callbacks != null ? callbacks : List.of()));
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
                    AgentProperties.ModelConfig modelConfig = routerConfig.intentClassifierModel();
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

    private String resolveModelName(AgentChatCommand command, ChatModelProvider provider) {
        if (command.modelName() != null) {
            return command.modelName();
        }
        if (StringUtils.hasText(command.modelType())) {
            return provider.getDefaultModelName();
        }
        AgentProperties.ModelConfig defaultModel = properties.defaultModel();
        return defaultModel.name() != null ? defaultModel.name() : provider.getDefaultModelName();
    }

    private ModelType resolveModelType(String modelType) {
        if (!StringUtils.hasText(modelType)) {
            return null;
        }
        try {
            return ModelType.fromCode(modelType);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown modelType: {}", modelType);
            return null;
        }
    }

    private List<ChatMessage> buildMessages(AgentChatCommand command) {
        List<ChatMessage> messages = new ArrayList<>();

        if (command.history() != null && !command.history().isEmpty()) {
            command.history().stream()
                    .map(this::toChatMessage)
                    .forEach(messages::add);
        }

        messages.add(ChatMessage.user(command.message()));
        return messages;
    }

    private ChatMessage toChatMessage(AgentChatCommand.HistoryMessage historyMessage) {
        if (historyMessage == null) {
            return null;
        }
        ChatMessage.Role role = switch (historyMessage.role()) {
            case SYSTEM -> ChatMessage.Role.SYSTEM;
            case USER -> ChatMessage.Role.USER;
            case ASSISTANT -> ChatMessage.Role.ASSISTANT;
            case TOOL -> ChatMessage.Role.TOOL;
        };
        return new ChatMessage(role, historyMessage.content());
    }
}
