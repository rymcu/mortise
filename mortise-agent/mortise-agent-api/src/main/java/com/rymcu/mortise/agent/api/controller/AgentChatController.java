package com.rymcu.mortise.agent.api.controller;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.agent.entity.Conversation;
import com.rymcu.mortise.agent.model.AgentModelInfo;
import com.rymcu.mortise.agent.model.ChatRequest;
import com.rymcu.mortise.agent.model.ChatResponse;
import com.rymcu.mortise.agent.model.ModelType;
import com.rymcu.mortise.agent.service.AgentService;
import com.rymcu.mortise.agent.service.AiModelService;
import com.rymcu.mortise.agent.service.AiProviderService;
import com.rymcu.mortise.agent.service.ConversationService;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.ApiController;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.rymcu.mortise.agent.entity.table.AiModelTableDef.AI_MODEL;
import static com.rymcu.mortise.agent.entity.table.AiProviderTableDef.AI_PROVIDER;
import static com.rymcu.mortise.agent.entity.table.ConversationTableDef.CONVERSATION;

/**
 * Mortise Agent API
 */
@Slf4j
@ApiController
@RequestMapping("/agent")
public class AgentChatController {

    private final AgentService agentService;
    private final ConversationService conversationService;
    private final AiProviderService aiProviderService;
    private final AiModelService aiModelService;

    public AgentChatController(
            AgentService agentService,
            ConversationService conversationService,
            @Qualifier("aiProviderServiceImpl") AiProviderService aiProviderService,
            @Qualifier("aiModelServiceImpl") AiModelService aiModelService
    ) {
        this.agentService = agentService;
        this.conversationService = conversationService;
        this.aiProviderService = aiProviderService;
        this.aiModelService = aiModelService;
    }

    /**
     * 获取可用模型列表（已启用的提供商及其下已启用的模型）
     */
    @GetMapping("/models")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<List<AgentModelInfo>> listModels() {
        QueryWrapper providerQuery = QueryWrapper.create()
                .where(AI_PROVIDER.STATUS.eq(Status.ENABLED.getCode()))
                .orderBy(AI_PROVIDER.SORT_NO.asc());
        List<AgentModelInfo> result = aiProviderService.list(providerQuery).stream().map(provider -> {
            QueryWrapper modelQuery = QueryWrapper.create()
                    .where(AI_MODEL.PROVIDER_ID.eq(provider.getId()))
                    .and(AI_MODEL.STATUS.eq(Status.ENABLED.getCode()))
                    .orderBy(AI_MODEL.SORT_NO.asc());
            List<AgentModelInfo.ModelItem> models = aiModelService.list(modelQuery).stream()
                    .map(m -> new AgentModelInfo.ModelItem(m.getModelName(), m.getDisplayName()))
                    .toList();
            return new AgentModelInfo(provider.getCode(), provider.getName(), models);
        }).toList();
        return GlobalResult.success(result);
    }

    /**
     * 获取当前用户的会话列表（按更新时间倒序）
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<List<Conversation>> listConversations(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(CONVERSATION.USER_ID.eq(currentUser.getUserId()))
                .and(CONVERSATION.STATUS.eq(Status.ENABLED.getCode()))
                .orderBy(CONVERSATION.UPDATED_TIME.desc());
        List<Conversation> conversations = conversationService.list(query);
        return GlobalResult.success(conversations);
    }

    /**
     * 删除会话（逻辑删除）
     */
    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<Void> deleteConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        conversationService.validateOwnership(id, currentUser.getUserId());
        conversationService.removeById(id);
        return GlobalResult.success(null);
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        Conversation conversation = conversationService.getOrCreate(
                request.conversationId(), currentUser.getUserId(), request.message());
        String conversationId = String.valueOf(conversation.getId());

        ChatResponse response = agentService.chat(request);
        return GlobalResult.success(response.withConversationId(conversationId));
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter chatStream(
            @RequestParam("message") String message,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "modelType", required = false) String modelType,
            @RequestParam(value = "modelName", required = false) String modelName,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        Conversation conversation = conversationService.getOrCreate(
                conversationId, currentUser.getUserId(), message);
        String resolvedConversationId = String.valueOf(conversation.getId());

        SseEmitter emitter = new SseEmitter(0L);
        ModelType resolvedType = resolveModelType(modelType);

        ChatRequest request = ChatRequest.builder()
            .message(message)
            .conversationId(resolvedConversationId)
            .modelType(resolvedType)
            .modelName(modelName)
            .build();

        emitter.onTimeout(() -> log.warn("Agent SSE timeout for conversationId={}", resolvedConversationId));
        emitter.onError(ex -> log.warn("Agent SSE error: {}", ex.getMessage()));

        CompletableFuture.runAsync(() -> {
            try {
                ChatResponse response = agentService.chat(request);
                ChatResponse withConversation = response.withConversationId(resolvedConversationId);
                emitter.send(SseEmitter.event().name("message").data(withConversation));
                emitter.send(SseEmitter.event().name("done").data("ok"));
                emitter.complete();
            } catch (Exception ex) {
                log.warn("Agent SSE failed: {}", ex.getMessage(), ex);
                try {
                    emitter.send(SseEmitter.event().name("error").data(
                            ex.getMessage() != null ? ex.getMessage() : "服务内部错误"));
                    emitter.complete();
                } catch (Exception sendEx) {
                    log.debug("Failed to send SSE error event: {}", sendEx.getMessage());
                    try {
                        emitter.completeWithError(ex);
                    } catch (Exception ignored) {
                        // emitter 可能已被容器回收
                    }
                }
            }
        });

        return emitter;
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
}
