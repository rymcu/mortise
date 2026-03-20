package com.rymcu.mortise.agent.api.controller;

import com.rymcu.mortise.agent.entity.Conversation;
import com.rymcu.mortise.agent.model.ChatRequest;
import com.rymcu.mortise.agent.model.ChatResponse;
import com.rymcu.mortise.agent.model.ModelType;
import com.rymcu.mortise.agent.service.AgentService;
import com.rymcu.mortise.agent.service.ConversationService;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.ApiController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * Mortise Agent API
 */
@Slf4j
@ApiController
@RequestMapping("/app/agent")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentService agentService;
    private final ConversationService conversationService;

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
                    emitter.send(SseEmitter.event().name("error").data(ex.getMessage()));
                } catch (Exception sendEx) {
                    log.debug("Failed to send SSE error event: {}", sendEx.getMessage());
                }
                emitter.completeWithError(ex);
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
