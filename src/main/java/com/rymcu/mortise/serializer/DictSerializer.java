package com.rymcu.mortise.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.rymcu.mortise.annotation.DictInterceptor;
import com.rymcu.mortise.service.DictService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Created on 2025/3/20 22:24.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.serializer
 */
@Component
public class DictSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private final DictInterceptor interceptor;

    @Resource
    private DictService dictService;

    public DictSerializer() {
        this(null);
    }

    public DictSerializer(DictInterceptor interceptor) {
        this.interceptor = interceptor;
        System.out.println("创建 DictSerializer: dictTypeCode=" +
                (interceptor != null ? interceptor.dictTypeCode() : "null"));
    }

    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        String fieldName = jsonGenerator.getOutputContext().getCurrentName();
        System.out.println("DictSerializer.serialize 调用，字段：" + fieldName + ", 值: " + value);

        // 检查 dictService 是否为 null
        if (dictService == null) {
            System.out.println("警告: dictService 为 null，无法查询字典");
            jsonGenerator.writeObject(value);
            return;
        }

        // 检查 interceptor 是否为 null
        if (interceptor == null) {
            System.out.println("警告: interceptor 为 null，无法获取字典类型");
            jsonGenerator.writeObject(value);
            return;
        }
        // 正常序列化原始字段值
        jsonGenerator.writeObject(value);

        // 生成字典文本字段
        String dictTypeCode = interceptor.dictTypeCode();
        String textFieldName = fieldName + interceptor.suffix();
        System.out.println("查询字典: 类型=" + dictTypeCode + ", 值=" + value);

        try {
            // 将字段值转为字符串，然后查询对应的字典标签
            String label = dictService.findLabelByTypeCodeAndValue(dictTypeCode, String.valueOf(value));
            System.out.println("查询结果: 标签=" + label);

            // 生成额外的文本字段
            if (StringUtils.hasText(label)) {
                jsonGenerator.writeStringField(textFieldName, label);
                System.out.println("写入字段: " + textFieldName + "=" + label);
            } else {
                // 如果找不到对应的标签，可以选择不输出或输出默认值
                jsonGenerator.writeStringField(textFieldName, String.valueOf(value));
                System.out.println("未找到标签，使用原值: " + textFieldName + "=" + value);
            }
        } catch (Exception e) {
            System.out.println("异常: " + e.getMessage());
            // 处理查询字典过程中的异常，避免序列化失败
            jsonGenerator.writeStringField(textFieldName, String.valueOf(value));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            DictInterceptor dict = beanProperty.getAnnotation(DictInterceptor.class);
            if (dict != null) {
                return new DictSerializer(dict);
            }
        }
        return this;
    }
}
