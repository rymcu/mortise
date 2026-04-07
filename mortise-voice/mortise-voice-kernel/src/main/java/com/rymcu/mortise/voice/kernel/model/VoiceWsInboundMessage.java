package com.rymcu.mortise.voice.kernel.model;

/**
 * WebSocket 入站消息。
 */
public record VoiceWsInboundMessage(
        String event,
        String sessionId,
        String payload
) {
}