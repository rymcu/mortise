package com.rymcu.mortise.voice.admin.contract.query;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * 语音任务查询条件。
 */
@Getter
@Setter
public class VoiceJobSearch extends BaseSearch {

    private String jobStatus;

    private String jobType;

    private Long profileId;
}