package com.rymcu.mortise.voice.kernel.model;

/**
 * WebSocket 出站消息。
 */
public record VoiceWsOutboundMessage(
        String event,
        String sessionId,
        String payload,
        boolean finalEvent
) {
}