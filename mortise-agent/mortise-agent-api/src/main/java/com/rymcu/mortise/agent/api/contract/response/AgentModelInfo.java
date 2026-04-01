package com.rymcu.mortise.agent.api.contract.response;

import java.util.List;

/**
 * 可用模型信息。
 */
public record AgentModelInfo(
        String providerCode,
        String providerName,
        List<ModelItem> models
) {

    public record ModelItem(
            String modelName,
            String displayName
    ) {
    }
}
