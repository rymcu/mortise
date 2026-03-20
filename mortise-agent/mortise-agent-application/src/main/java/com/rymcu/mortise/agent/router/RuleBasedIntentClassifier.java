package com.rymcu.mortise.agent.router;

import com.rymcu.mortise.agent.constant.AgentConstants;
import com.rymcu.mortise.agent.model.AgentIntent;
import com.rymcu.mortise.agent.model.IntentResult;
import com.rymcu.mortise.agent.spi.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 基于规则的意图分类器
 * 使用关键词匹配快速判断意图，作为轻量级的备选方案
 */
public class RuleBasedIntentClassifier implements IntentClassifier {
    
    private static final Logger log = LoggerFactory.getLogger(RuleBasedIntentClassifier.class);
    
    private static final Set<String> TOOL_KEYWORDS = Set.of(
        "帮我", "执行", "运行", "调用", "处理", "操作",
        "查询", "搜索", "查找", "获取", "创建", "删除", "修改", "更新",
        "发送", "通知", "提醒", "计算", "分析", "生成", "导出"
    );
    
    private static final Set<String> CHAT_KEYWORDS = Set.of(
        "你好", "在吗", "是什么", "怎么样", "什么是", "为什么", "如何",
        "聊聊", "谈谈", "解释", "说明", "介绍"
    );
    
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^[什么|如何|为什么|怎么|哪].*[？?]$");
    
    @Override
    public IntentResult classify(String userMessage, List<String> availableTools) {
        String message = userMessage.toLowerCase().trim();
        
        // 检查是否有明确的工具调用意图
        for (String keyword : TOOL_KEYWORDS) {
            if (message.contains(keyword)) {
                List<String> matchedTools = findMatchingTools(message, availableTools);
                if (!matchedTools.isEmpty()) {
                    log.debug("Rule matched tool intent with keyword: {}", keyword);
                    return IntentResult.toolCall(AgentConstants.RULE_TOOL_KEYWORD_CONFIDENCE, "Matched keyword: " + keyword, matchedTools);
                }
            }
        }
        
        // 检查是否是纯聊天
        for (String keyword : CHAT_KEYWORDS) {
            if (message.contains(keyword)) {
                log.debug("Rule matched chat intent with keyword: {}", keyword);
                return IntentResult.chat(AgentConstants.RULE_CHAT_KEYWORD_CONFIDENCE, "Matched chat keyword: " + keyword);
            }
        }
        
        // 检查问句模式
        if (QUESTION_PATTERN.matcher(message).matches()) {
            log.debug("Rule matched question pattern");
            return IntentResult.chat(AgentConstants.RULE_QUESTION_PATTERN_CONFIDENCE, "Matched question pattern");
        }
        
        // 默认返回聊天意图
        return IntentResult.chat(AgentConstants.RULE_DEFAULT_CONFIDENCE, "No specific pattern matched, defaulting to chat");
    }
    
    @Override
    public String getName() {
        return "rule-based-intent-classifier";
    }
    
    private List<String> findMatchingTools(String message, List<String> availableTools) {
        return availableTools.stream()
            .filter(tool -> message.contains(tool.toLowerCase()) || 
                           message.contains(tool.replace("_", "").toLowerCase()))
            .limit(3)
            .toList();
    }
}
