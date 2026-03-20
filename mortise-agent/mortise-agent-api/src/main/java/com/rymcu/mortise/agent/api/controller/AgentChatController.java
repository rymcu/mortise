package com.rymcu.mortise.agent.api.controller;

import com.rymcu.mortise.agent.model.ChatRequest;
import com.rymcu.mortise.agent.model.ChatResponse;
import com.rymcu.mortise.agent.model.ModelType;
import com.rymcu.mortise.agent.service.AgentService;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.ApiController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    @PostMapping("/chat")
    public GlobalResult<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return GlobalResult.success(agentService.chat(request));
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam("message") String message,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "modelType", required = false) String modelType,
            @RequestParam(value = "modelName", required = false) String modelName
    ) {
        SseEmitter emitter = new SseEmitter(0L);
        ModelType resolvedType = resolveModelType(modelType);

        ChatRequest request = ChatRequest.builder()
            .message(message)
            .conversationId(conversationId)
            .modelType(resolvedType)
            .modelName(modelName)
            .build();

        emitter.onTimeout(() -> log.warn("Agent SSE timeout for conversationId={}", conversationId));
        emitter.onError(ex -> log.warn("Agent SSE error: {}", ex.getMessage()));

        CompletableFuture.runAsync(() -> {
            try {
                ChatResponse response = agentService.chat(request);
                emitter.send(SseEmitter.event().name("message").data(response));
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
