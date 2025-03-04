package com.rymcu.mortise.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 配置类
 * @author ronger
 */
@Configuration
public class JacksonConfig {

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();

        // 配置序列化规则 - 设置为不包含null值，这会覆盖废弃的WRITE_NULL_MAP_VALUES设置
        builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                // 如果有未知属性，不抛出异常
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 允许出现特殊字符和转义符
                .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true)
                // 允许出现单引号
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                // 允许出现换行符等
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                // 允许注释
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                // 空对象不报错
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // 时间格式序列化为字符串，不用时间戳
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                // 枚举类序列化为字符串
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                // 允许使用空值反序列化
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                // 使用快速解析器
                .configure(JsonParser.Feature.USE_FAST_DOUBLE_PARSER, true)
                // 允许反斜杠转义任何字符
                .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        JsonMapper jsonMapper = builder.build();

        // 注册 JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 处理LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));

        // 处理LocalDate
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));

        // 处理LocalTime
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        // 注册数值处理模块
        SimpleModule numberModule = new SimpleModule();
        // 处理BigDecimal
        numberModule.addSerializer(BigDecimal.class, new JsonSerializer<BigDecimal>() {
            @Override
            public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    // 输出两位小数
                    gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toString());
                }
            }
        });

        // 处理Double
        numberModule.addSerializer(Double.class, new JsonSerializer<Double>() {
            @Override
            public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    // 输出两位小数
                    gen.writeString(BigDecimal.valueOf(value)
                            .setScale(2, RoundingMode.HALF_UP).toString());
                }
            }
        });

        // 处理Float
        numberModule.addSerializer(Float.class, new JsonSerializer<Float>() {
            @Override
            public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    // 输出两位小数
                    gen.writeString(BigDecimal.valueOf(value)
                            .setScale(2, RoundingMode.HALF_UP).toString());
                }
            }
        });

        // 处理Long类型为字符串
        numberModule.addSerializer(Long.class, ToStringSerializer.instance);
        numberModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 注册所有模块
        jsonMapper.registerModule(javaTimeModule);
        jsonMapper.registerModule(numberModule);

        return jsonMapper;
    }
}
