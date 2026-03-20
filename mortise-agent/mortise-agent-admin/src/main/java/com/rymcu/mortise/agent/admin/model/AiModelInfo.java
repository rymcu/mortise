package com.rymcu.mortise.agent.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * AI 模型信息 VO
 *
 * @author ronger
 */
public record AiModelInfo(
    Long id,
    Long providerId,
    String providerName,
    String modelName,
    String displayName,
    Integer status,
    Integer sortNo,
    String remark,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedTime
) {
}
