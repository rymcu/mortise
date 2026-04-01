package com.rymcu.mortise.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 模型领域对象。
 */
@Data
public class AiModel {

    private Long id;

    private Long providerId;

    private String modelName;

    private String displayName;

    private Integer status;

    private Integer sortNo;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
