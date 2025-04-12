package com.rymcu.mortise.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rymcu.mortise.service.DictService;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created on 2025/4/12 17:33.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.serializer
 */
public class DictSerializer<T> extends JsonSerializer<T> {
    private final DictService dictService;
    @Setter
    private String dictType;

    public DictSerializer(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            String code = value.toString();
            String translatedText = dictService.findLabelByTypeCodeAndValue(dictType, code);
            gen.writeString(StringUtils.isBlank(translatedText) ? code : translatedText);
        } else {
            gen.writeNull();
        }
    }
}
