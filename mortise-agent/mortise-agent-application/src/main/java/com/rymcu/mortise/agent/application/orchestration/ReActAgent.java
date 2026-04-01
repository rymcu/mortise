package com.rymcu.mortise.agent.application.orchestration;

import com.rymcu.mortise.agent.application.prompt.AgentPromptTemplate;
import com.rymcu.mortise.agent.application.prompt.PromptBuilder;
import com.rymcu.mortise.agent.kernel.model.AgentIntent;
import com.rymcu.mortise.agent.kernel.model.ChatMessage;
import com.rymcu.mortise.agent.kernel.model.ChatResponse;
import com.rymcu.mortise.agent.kernel.spi.ChatModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct Agent 实现
 * 实现 ReAct (Reasoning + Acting) 模式的 Agent
 * 使用 Spring AI 原生的 Function Calling 机制
 */
public class ReActAgent {
    
    private static final Logger log = LoggerFactory.getLogger(ReActAgent.class);
    
    private final ChatModelProvider modelProvider;
    private final int maxIterations;
    private final boolean enableSelfReflection;
    private final AgentPromptTemplate promptTemplate;
    
    public ReActAgent(
            ChatModelProvider modelProvider,
            AgentPromptTemplate promptTemplate,
            int maxIterations,
            boolean enableSelfReflection
    ) {
        this.modelProvider = modelProvider;
        this.promptTemplate = promptTemplate;
        this.maxIterations = maxIterations;
        this.enableSelfReflection = enableSelfReflection;
    }
    
    /**
     * 执行 ReAct 循环
     * 
     * @param messages 消息列表
     * @param modelName 模型名称
     * @param functionCallbacks Spring AI FunctionCallback 列表
     */
    public ChatResponse execute(
            List<ChatMessage> messages, 
            String modelName, 
            List<FunctionCallback> functionCallbacks) {
        
        List<ChatMessage> conversationHistory = new ArrayList<>(messages);
        
        // 提取用户问题
        String question = extractLastUserMessage(messages);
        
        // 构建提示词
        PromptBuilder promptBuilder = PromptBuilder.create(promptTemplate)
            .question(question)
            .maxSteps(maxIterations);
        
        // 添加系统提示
        String systemPrompt = promptBuilder.currentStep(1).buildReActPrompt();
        conversationHistory.add(0, ChatMessage.system(systemPrompt));
        
        int iteration = 0;
        while (iteration < maxIterations) {
            iteration++;
            log.debug("ReAct iteration {}/{}", iteration, maxIterations);
            
            // 更新当前步骤提示
            if (iteration > 1) {
                systemPrompt = promptBuilder.currentStep(iteration).buildReActPrompt();
                conversationHistory.set(0, ChatMessage.system(systemPrompt));
            }
            
            // 调用模型（Spring AI 会自动处理 Function Calling）
            ChatResponse response = modelProvider.chatWithFunctions(
                conversationHistory, 
                modelName, 
                functionCallbacks
            );
            
            // 检查响应
            if (response.content() != null && !response.content().isEmpty()) {
                // 模型返回了最终答案
                return ChatResponse.builder()
                    .content(response.content())
                    .intent(AgentIntent.TOOL_CALL)
                    .modelType(response.modelType())
                    .modelName(response.modelName())
                    .tokenUsage(response.tokenUsage())
                    .build();
            }
            
            // 如果响应为空但还有迭代次数，继续
            if (enableSelfReflection && iteration < maxIterations) {
                continue;
            }
        }
        
        // 达到最大迭代次数
        return ChatResponse.builder()
            .content("I've completed the task within the allowed steps.")
            .intent(AgentIntent.TOOL_CALL)
            .modelType(modelProvider.getModelType())
            .modelName(modelName)
            .build();
    }
    
    private String extractLastUserMessage(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).role() == ChatMessage.Role.USER) {
                return messages.get(i).content();
            }
        }
        return "";
    }
}
