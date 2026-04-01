package com.rymcu.mortise.agent.application.command;

/**
 * AI 提供商新增/更新命令。
 */
public record AiProviderUpsertCommand(
        String name,
        String code,
        String apiKey,
        String baseUrl,
        String defaultModelName,
        Integer status,
        Integer sortNo,
        String remark
) {
}
