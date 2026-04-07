package com.rymcu.mortise.voice.application.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.common.exception.BusinessException;

/**
 * 语音目录配置校验支持。
 */
public final class VoiceCatalogValidationSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private VoiceCatalogValidationSupport() {
    }

    public static String normalizeOptionalJsonObject(String rawJson, String fieldName) {
        if (rawJson == null) {
            return null;
        }
        String normalized = rawJson.strip();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(normalized);
            if (jsonNode == null || !jsonNode.isObject()) {
                throw new BusinessException(fieldName + "必须是 JSON 对象");
            }
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(fieldName + "不是合法 JSON");
        }
    }
}