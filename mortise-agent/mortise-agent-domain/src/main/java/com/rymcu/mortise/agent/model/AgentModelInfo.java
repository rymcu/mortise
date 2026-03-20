package com.rymcu.mortise.agent.model;

import java.util.List;

/**
 * 前端可用模型列表响应
 * <p>
 * 按提供商分组，每个提供商包含其下已启用的模型列表。
 * 供 Agent Chat 页面动态渲染模型选择器使用。
 * </p>
 *
 * @param providerCode 提供商编码（对应 chat/stream 接口的 modelType 参数）
 * @param providerName 提供商显示名称
 * @param models       该提供商下已启用的模型列表
 */
public record AgentModelInfo(
        String providerCode,
        String providerName,
        List<ModelItem> models
) {

    /**
     * 单个模型信息
     *
     * @param modelName   模型名称（对应 chat/stream 接口的 modelName 参数）
     * @param displayName 模型显示名称
     */
    public record ModelItem(
            String modelName,
            String displayName
    ) {
    }
}
