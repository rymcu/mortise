package com.rymcu.mortise.voice.admin.contract.query;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * 语音模型查询条件。
 */
@Getter
@Setter
public class VoiceModelSearch extends BaseSearch {

    private Long providerId;

    private String capability;
}