package com.rymcu.mortise.system.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rymcu.mortise.system.model.DictInfo;
import com.rymcu.mortise.system.service.DictService;
import lombok.Setter;

import java.io.IOException;
import java.util.Objects;

/**
 * Created on 2025/4/12 17:33.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.serializer
 */
public class DictSerializer extends JsonSerializer<Object> {
    private final DictService dictService;
    @Setter
    private String dictType;
    @Setter
    private String suffix;
    @Setter
    private boolean cover;

    public DictSerializer(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        if (value != null) {
            DictInfo dictInfo = dictService.findDictInfo(dictType, String.valueOf(value));
            if (Objects.nonNull(dictInfo)) {
                if (cover) {
                    jsonGenerator.writeObject(dictInfo);
                } else {
                    jsonGenerator.writeObject(value);
                    String fieldName = jsonGenerator.getOutputContext().getCurrentName();
                    String textFieldName = fieldName + suffix;
                    jsonGenerator.writeObjectField(textFieldName, dictInfo.getLabel());
                }
            } else {
                jsonGenerator.writeObject(value);
            }
        } else {
            jsonGenerator.writeNull();
        }
    }
}
