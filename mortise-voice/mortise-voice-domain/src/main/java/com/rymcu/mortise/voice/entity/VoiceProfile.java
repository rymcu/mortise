package com.rymcu.mortise.voice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语音配置组合领域对象。
 */
@Data
public class VoiceProfile {

    private Long id;

    private String name;

    private String code;

    private String language;

    private Long asrProviderId;

    private Long asrModelId;

    private Long vadProviderId;

    private Long vadModelId;

    private Long ttsProviderId;

    private Long ttsModelId;

    private String defaultParams;

    private Integer status;

    private Integer sortNo;

    private String remark;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}