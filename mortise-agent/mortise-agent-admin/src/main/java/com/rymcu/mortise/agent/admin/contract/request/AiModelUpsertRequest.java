package com.rymcu.mortise.agent.admin.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 模型新增/更新请求。
 */
public record AiModelUpsertRequest(
        @NotNull Long providerId,
        @NotBlank String modelName,
        @NotBlank String displayName,
        Integer status,
        Integer sortNo,
        String remark
) {
}
