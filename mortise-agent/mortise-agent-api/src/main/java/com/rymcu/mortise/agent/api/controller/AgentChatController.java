package com.rymcu.mortise.agent.api.controller;

import com.rymcu.mortise.agent.api.contract.request.AgentChatRequest;
import com.rymcu.mortise.agent.api.contract.response.AgentChatResponse;
import com.rymcu.mortise.agent.api.contract.response.AgentModelInfo;
import com.rymcu.mortise.agent.api.contract.response.ConversationInfo;
import com.rymcu.mortise.agent.api.facade.AgentChatFacade;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.ApiController;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

/**
 * Mortise Agent API
 */
@Slf4j
@ApiController
@RequestMapping("/agent")
public class AgentChatController {

    private final AgentChatFacade agentChatFacade;

    public AgentChatController(
            AgentChatFacade agentChatFacade
    ) {
        this.agentChatFacade = agentChatFacade;
    }

    /**
     * 获取可用模型列表（已启用的提供商及其下已启用的模型）
     */
    @GetMapping("/models")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<List<AgentModelInfo>> listModels() {
        return GlobalResult.success(agentChatFacade.listModels());
    }

    /**
     * 获取当前用户的会话列表（按更新时间倒序）
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<List<ConversationInfo>> listConversations(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return GlobalResult.success(agentChatFacade.listConversations(currentUser.getUserId()));
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
        agentChatFacade.deleteConversation(id, currentUser.getUserId());
        return GlobalResult.success(null);
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public GlobalResult<AgentChatResponse> chat(
            @Valid @RequestBody AgentChatRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return GlobalResult.success(agentChatFacade.chat(request, currentUser.getUserId()));
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
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onTimeout(() -> log.warn("Agent SSE timeout for conversationId={}", conversationId));
        emitter.onError(ex -> log.warn("Agent SSE error: {}", ex.getMessage()));

        CompletableFuture.runAsync(() -> {
            try {
                AgentChatResponse response = agentChatFacade.chat(
                        message,
                        conversationId,
                        modelType,
                        modelName,
                        currentUser.getUserId()
                );
                emitter.send(SseEmitter.event().name("message").data(response));
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
}
