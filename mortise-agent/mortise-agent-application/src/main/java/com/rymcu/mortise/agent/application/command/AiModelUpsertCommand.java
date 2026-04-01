package com.rymcu.mortise.agent.application.command;

/**
 * AI 模型新增/更新命令。
 */
public record AiModelUpsertCommand(
        Long providerId,
        String modelName,
        String displayName,
        Integer status,
        Integer sortNo,
        String remark
) {
}
