package com.rymcu.mortise.voice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语音提供商领域对象。
 */
@Data
public class VoiceProvider {

    private Long id;

    private String name;

    private String code;

    private String providerType;

    private Integer status;

    private Integer sortNo;

    private String defaultConfig;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}