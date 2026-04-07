package com.rymcu.mortise.voice.kernel.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行时节点状态。
 */
public record VoiceRuntimeNodeStatus(
        String nodeId,
        String baseUrl,
        String configStatus,
        String probeStatus,
        String detail,
        Long latencyMillis,
        LocalDateTime checkedTime,
        List<String> loadedModels
) {
}