package com.rymcu.mortise.agent.admin.contract.request;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 提供商新增/更新请求。
 */
public record AiProviderUpsertRequest(
        @NotBlank String name,
        @NotBlank String code,
        String apiKey,
        String baseUrl,
        String defaultModelName,
        Integer status,
        Integer sortNo,
        String remark
) {
}
