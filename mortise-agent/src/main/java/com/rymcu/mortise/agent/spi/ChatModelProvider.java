package com.rymcu.mortise.agent.spi;

import com.rymcu.mortise.agent.model.ChatMessage;
import com.rymcu.mortise.agent.model.ChatResponse;
import com.rymcu.mortise.agent.model.ModelType;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.List;

/**
 * 聊天模型提供者接口
 * 统一不同 AI 模型的调用方式
 */
public interface ChatModelProvider {
    
    /**
     * 获取支持的模型类型
     */
    ModelType getModelType();
    
    /**
     * 检查此提供者是否可用（API Key 已配置等）
     */
    boolean isAvailable();
    
    /**
     * 执行普通聊天（无工具调用）
     *
     * @param messages 消息列表
     * @param modelName 模型名称
     * @return 聊天响应
     */
    ChatResponse chat(List<ChatMessage> messages, String modelName);
    
    /**
     * 执行聊天并支持函数调用
     *
     * @param messages 消息列表
     * @param modelName 模型名称
     * @param functionCallbacks Spring AI FunctionCallback 列表
     * @return 聊天响应（可能包含工具调用请求）
     */
    ChatResponse chatWithFunctions(List<ChatMessage> messages, String modelName, List<FunctionCallback> functionCallbacks);
    
    /**
     * 获取默认模型名称
     */
    String getDefaultModelName();
    
    /**
     * 获取可用模型列表
     */
    List<String> getAvailableModels();
}