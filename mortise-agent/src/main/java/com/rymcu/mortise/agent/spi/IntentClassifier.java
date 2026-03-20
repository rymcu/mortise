package com.rymcu.mortise.agent.spi;

import com.rymcu.mortise.agent.model.IntentResult;
import java.util.List;

/**
 * 意图分类器接口
 * 负责分析用户输入并判断意图类型
 */
public interface IntentClassifier {
    
    /**
     * 分类用户意图
     *
     * @param userMessage 用户消息
     * @param availableTools 可用工具列表（用于判断是否需要工具调用）
     * @return 意图识别结果
     */
    IntentResult classify(String userMessage, List<String> availableTools);
    
    /**
     * 获取分类器名称
     */
    String getName();
}
