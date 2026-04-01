package com.rymcu.mortise.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 提供商领域对象。
 */
@Data
public class AiProvider {

    private Long id;

    private String name;

    private String code;

    private String apiKey;

    private String baseUrl;

    private String defaultModelName;

    private Integer status;

    private Integer sortNo;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
