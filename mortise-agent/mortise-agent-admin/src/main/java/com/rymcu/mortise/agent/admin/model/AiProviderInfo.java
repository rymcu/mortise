package com.rymcu.mortise.agent.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * AI 提供商信息 VO
 *
 * @author ronger
 */
public record AiProviderInfo(
    Long id,
    String name,
    String code,
    String baseUrl,
    String defaultModelName,
    Integer status,
    Integer sortNo,
    String remark,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedTime
) {
}
