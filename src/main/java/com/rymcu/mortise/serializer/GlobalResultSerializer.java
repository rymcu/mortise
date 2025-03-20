package com.rymcu.mortise.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rymcu.mortise.core.result.GlobalResult;

import java.io.IOException;

/**
 * Created on 2025/3/20 23:42.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.serializer
 */
public class GlobalResultSerializer extends JsonSerializer<GlobalResult<?>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<GlobalResult<?>> handledType() {
        return (Class<GlobalResult<?>>) (Class<?>) GlobalResult.class;
    }

    @Override
    public void serialize(GlobalResult<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 处理 GlobalResult，确保其中的 data 也被正确序列化
        gen.writeStartObject();
        gen.writeNumberField("code", value.getCode());
        gen.writeStringField("message", value.getMessage());

        // 处理 data 字段
        gen.writeFieldName("data");
        Object data = value.getData();
        // 确保 data 中的对象也被正确序列化
        if (data != null) {
            serializers.defaultSerializeValue(data, gen);
        } else {
            gen.writeNull();
        }

        gen.writeEndObject();
    }
}
