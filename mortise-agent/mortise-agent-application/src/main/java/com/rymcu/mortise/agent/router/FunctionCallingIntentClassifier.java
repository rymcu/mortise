package com.rymcu.mortise.agent.router;

import com.rymcu.mortise.agent.constant.AgentConstants;
import com.rymcu.mortise.agent.model.IntentResult;
import com.rymcu.mortise.agent.model.ChatMessage;
import com.rymcu.mortise.agent.spi.ChatModelProvider;
import com.rymcu.mortise.agent.spi.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基于 Function Calling 的意图分类器
 * 
 * 核心原理：
 * 1. 注册一个意图分类函数作为 FunctionCallback
 * 2. 发送用户消息给模型，让模型决定是否调用这个函数
 * 3. 如果模型直接返回文本 → CHAT 意图（普通聊天）
 * 4. 如果模型调用意图分类函数 → TOOL_CALL 意图（需要工具执行）
 * 
 * Spring AI 会自动处理函数调用，我们只需要检测函数是否被调用
 */
public class FunctionCallingIntentClassifier implements IntentClassifier {
    
    private static final Logger log = LoggerFactory.getLogger(FunctionCallingIntentClassifier.class);
    
    private final ChatModelProvider modelProvider;
    
    private static final String INTENT_FUNCTION_NAME = AgentConstants.INTENT_FUNCTION_NAME;
    
    /**
     * 系统提示：引导模型使用函数调用来表示意图
     */
    private static final String SYSTEM_PROMPT = """
        You are an intent classifier for an AI assistant.
        
        Your job is to determine if the user's message requires tool execution.
        
        ## When to call `indicate_tool_intent` function:
        - User wants to perform an action (calculate, search, send, create, delete, update)
        - User needs real-time data (current time, weather, stock prices)
        - User explicitly requests tool execution
        - User's request matches one of the available tools
        
        ## When to respond directly (DO NOT call function):
        - Greetings: "Hello", "Hi", "你好", "在吗"
        - General questions: "What is...", "Explain...", "Tell me about..."
        - Casual conversation
        - Knowledge-based Q&A that doesn't require tools
        
        ## Important:
        - If you're unsure, respond directly without calling the function
        - Only call the function when you're confident the user needs tool execution
        - Always respond in the same language as the user
        """;
    
    public FunctionCallingIntentClassifier(ChatModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }
    
    @Override
    public IntentResult classify(String userMessage, List<String> availableTools) {
        log.debug("Classifying intent using function calling for: {}", userMessage);
        
        // 用于捕获函数调用结果
        AtomicReference<ToolIntentInput> capturedInput = new AtomicReference<>();
        
        // 构建意图指示函数
        FunctionCallback intentFunction = buildIntentFunction(availableTools, capturedInput);
        
        // 构建消息
        List<ChatMessage> messages = List.of(
            ChatMessage.system(SYSTEM_PROMPT),
            ChatMessage.user(userMessage)
        );
        
        try {
            // 调用模型（Spring AI 会自动处理函数调用）
            modelProvider.chatWithFunctions(messages, null, List.of(intentFunction));
            
            // 检查函数是否被调用
            ToolIntentInput input = capturedInput.get();
            if (input != null) {
                // 函数被调用 → TOOL_CALL 意图
                log.debug("Intent function was called, classified as TOOL_CALL with tools: {}", 
                    input.recommendedTools());
                return IntentResult.toolCall(
                    input.confidence(), 
                    input.reasoning(), 
                    input.recommendedTools()
                );
            }
            
            // 函数未被调用，模型直接返回文本 → CHAT 意图
            log.debug("Intent function was NOT called, classified as CHAT");
            return IntentResult.chat(AgentConstants.FC_DIRECT_REPLY_CONFIDENCE, "Model responded directly without function call");
            
        } catch (Exception e) {
            log.warn("Function calling intent classification failed: {}", e.getMessage());
            return IntentResult.chat(AgentConstants.FALLBACK_CONFIDENCE, "Fallback to chat due to error: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "function-calling-intent-classifier";
    }
    
    /**
     * 构建意图指示函数
     * 当模型调用此函数时，表示用户需要工具执行
     */
    private FunctionCallback buildIntentFunction(
            List<String> availableTools, 
            AtomicReference<ToolIntentInput> capturedInput) {
        
        String toolListStr = availableTools.isEmpty() ? 
            "No tools available" : String.join(", ", availableTools);
        
        String description = """
            Call this function to indicate that the user's message requires tool execution.
            Available tools: %s
            
            ONLY call this when the user clearly needs to use one of these tools.
            Do NOT call for general questions or casual conversation.
            Input should be a JSON object with:
            - recommendedTools: array of tool names that should be used
            - reasoning: brief explanation of why this requires tool execution
            - confidence: confidence level between 0.0 and 1.0
            """.formatted(toolListStr);

        return FunctionCallback.builder()
            .function(INTENT_FUNCTION_NAME, (ToolIntentInput input) -> {
                // 捕获输入参数
                capturedInput.set(input);
                log.debug("Intent function called with: {}", input);
                // 返回确认，Spring AI 会将结果返回给模型
                return "Intent confirmed: tool_call with tools " + input.recommendedTools();
            })
            .description(description)
            .inputType(ToolIntentInput.class)
            .build();
    }
    
    /**
     * 意图指示函数的输入参数
     */
    public record ToolIntentInput(
        List<String> recommendedTools,
        String reasoning,
        double confidence
    ) {}
}