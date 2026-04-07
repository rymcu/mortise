package com.rymcu.mortise.voice.admin.contract.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端运行时节点信息。
 */
public record VoiceRuntimeNodeInfo(
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