package com.rymcu.mortise.voice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语音模型领域对象。
 */
@Data
public class VoiceModel {

    private Long id;

    private Long providerId;

    private String name;

    private String code;

    private String capability;

    private String modelType;

    private String runtimeName;

    private String version;

    private String language;

    private Integer concurrencyLimit;

    private Boolean defaultModel;

    private Integer status;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}