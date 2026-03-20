package com.rymcu.mortise.agent.router;

import com.rymcu.mortise.agent.constant.AgentConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.agent.model.AgentIntent;
import com.rymcu.mortise.agent.model.ChatMessage;
import com.rymcu.mortise.agent.model.ChatResponse;
import com.rymcu.mortise.agent.model.IntentResult;
import com.rymcu.mortise.agent.prompt.AgentPromptTemplate;
import com.rymcu.mortise.agent.prompt.PromptBuilder;
import com.rymcu.mortise.agent.spi.ChatModelProvider;
import com.rymcu.mortise.agent.spi.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 基于 LLM 的意图分类器
 * 使用大语言模型来识别用户意图
 */
public class LlmIntentClassifier implements IntentClassifier {
    
    private static final Logger log = LoggerFactory.getLogger(LlmIntentClassifier.class);
    
    private final ChatModelProvider modelProvider;
    private final ObjectMapper objectMapper;
    private final AgentPromptTemplate promptTemplate;
    
    public LlmIntentClassifier(ChatModelProvider modelProvider, AgentPromptTemplate promptTemplate) {
        this.modelProvider = modelProvider;
        this.objectMapper = new ObjectMapper();
        this.promptTemplate = promptTemplate;
    }
    
    @Override
    public IntentResult classify(String userMessage, List<String> availableTools) {
        String toolList = availableTools.isEmpty() ? "None" : String.join(", ", availableTools);
        
        String prompt = PromptBuilder.create(promptTemplate)
            .toolList(toolList)
            .userMessage(userMessage)
            .buildIntentClassificationPrompt();
        
        List<ChatMessage> messages = List.of(ChatMessage.user(prompt));
        
        ChatResponse response = modelProvider.chat(messages, null);
        String content = response.content();
        
        try {
            JsonNode json = objectMapper.readTree(extractJson(content));
            String intentCode = json.path("intent").asText("chat");
            double confidence = json.path("confidence").asDouble(AgentConstants.FALLBACK_CONFIDENCE);
            String reasoning = json.path("reasoning").asText("");
            List<String> tools = parseTools(json.path("recommendedTools"));
            
            AgentIntent intent = AgentIntent.fromCode(intentCode);
            log.debug("Classified intent: {}, confidence: {}, tools: {}", intent, confidence, tools);
            
            return new IntentResult(intent, confidence, reasoning, tools);
        } catch (Exception e) {
            log.warn("Failed to parse intent classification result: {}", e.getMessage());
            return IntentResult.chat(AgentConstants.FALLBACK_CONFIDENCE, "Failed to classify, defaulting to chat");
        }
    }
    
    @Override
    public String getName() {
        return "llm-intent-classifier";
    }
    
    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
    
    private List<String> parseTools(JsonNode toolsNode) {
        if (toolsNode.isArray()) {
            return objectMapper.convertValue(toolsNode, List.class);
        }
        return List.of();
    }
}