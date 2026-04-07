package com.rymcu.mortise.voice.model;

import java.util.List;

/**
 * 语音配置分页查询条件。
 */
public record VoiceProfileSearchCriteria(
        String query,
        Integer status,
        List<Long> providerIds,
        List<Long> modelIds
) {
}