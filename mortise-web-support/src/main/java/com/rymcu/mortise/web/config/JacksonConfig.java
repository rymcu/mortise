package com.rymcu.mortise.web.config;

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
import com.rymcu.mortise.web.spi.JacksonConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Jackson 配置类
 * 
 * <p>提供统一的 JSON 序列化/反序列化配置，支持通过 JacksonConfigurer SPI 扩展。</p>
 * 
 * <p><strong>核心特性：</strong></p>
 * <ul>
 *     <li>统一的日期时间处理</li>
 *     <li>数值类型精度控制</li>
 *     <li>容错的反序列化配置</li>
 *     <li>支持业务模块通过 SPI 扩展</li>
 * </ul>
 * 
 * <p><strong>扩展机制：</strong></p>
 * <ul>
 *     <li>实现 JacksonConfigurer 接口</li>
 *     <li>标注 @Component 注解</li>
 *     <li>Spring 会自动发现并应用配置</li>
 * </ul>
 * 
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class JacksonConfig {

    private final List<JacksonConfigurer> jacksonConfigurers;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public JacksonConfig(Optional<List<JacksonConfigurer>> configurersOptional) {
        this.jacksonConfigurers = configurersOptional.orElse(List.of());
        log.info("发现 {} 个 JacksonConfigurer 扩展", this.jacksonConfigurers.size());
        
        // 记录已发现的配置器
        this.jacksonConfigurers.forEach(configurer -> 
            log.info("注册 JacksonConfigurer: {} (order: {})", 
                    configurer.getClass().getSimpleName(), 
                    configurer.getOrder())
        );
    }

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        // 使用 JsonMapper.builder 创建基础配置
        JsonMapper.Builder builder = JsonMapper.builder()
                // 序列化配置
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                
                // 反序列化配置
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                
                // JSON 解析配置
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .configure(JsonParser.Feature.USE_FAST_DOUBLE_PARSER, true)
                .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true)
                .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        ObjectMapper objectMapper = builder.build();

        // 配置序列化包含策略
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 注册 Java 时间模块
        registerJavaTimeModule(objectMapper);

        // 注册数值处理模块
        registerNumberModule(objectMapper);

        // 应用 SPI 扩展配置
        applySpiConfigurations(objectMapper);

        log.info("ObjectMapper 配置完成，已应用 {} 个扩展配置器", jacksonConfigurers.size());
        return objectMapper;
    }

    /**
     * 注册 Java 时间模块
     */
    private void registerJavaTimeModule(ObjectMapper objectMapper) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));

        // LocalDate
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));

        // LocalTime
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        objectMapper.registerModule(javaTimeModule);
        log.debug("JavaTimeModule 已注册");
    }

    /**
     * 注册数值处理模块
     */
    private void registerNumberModule(ObjectMapper objectMapper) {
        SimpleModule numberModule = new SimpleModule();

        // BigDecimal - 保留两位小数
        numberModule.addSerializer(BigDecimal.class, new JsonSerializer<BigDecimal>() {
            @Override
            public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toString());
                }
            }
        });

        // Double - 保留两位小数
        numberModule.addSerializer(Double.class, new JsonSerializer<Double>() {
            @Override
            public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    gen.writeString(BigDecimal.valueOf(value)
                            .setScale(2, RoundingMode.HALF_UP).toString());
                }
            }
        });

        // Float - 保留两位小数
        numberModule.addSerializer(Float.class, new JsonSerializer<Float>() {
            @Override
            public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    gen.writeString(BigDecimal.valueOf(value)
                            .setScale(2, RoundingMode.HALF_UP).toString());
                }
            }
        });

        // Long - 转换为字符串（避免 JavaScript 精度丢失）
        numberModule.addSerializer(Long.class, ToStringSerializer.instance);
        numberModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        objectMapper.registerModule(numberModule);
        log.debug("NumberModule 已注册");
    }

    /**
     * 应用 SPI 扩展配置
     */
    private void applySpiConfigurations(ObjectMapper objectMapper) {
        if (jacksonConfigurers.isEmpty()) {
            log.debug("没有发现 JacksonConfigurer 扩展");
            return;
        }

        // 按 order 排序并应用配置
        jacksonConfigurers.stream()
                .filter(JacksonConfigurer::isEnabled)
                .sorted(Comparator.comparingInt(JacksonConfigurer::getOrder))
                .forEach(configurer -> {
                    try {
                        configurer.configureObjectMapper(objectMapper);
                        log.debug("应用 JacksonConfigurer: {} (order: {})", 
                                configurer.getClass().getSimpleName(), 
                                configurer.getOrder());
                    } catch (Exception e) {
                        log.error("应用 JacksonConfigurer 失败: {}", 
                                configurer.getClass().getSimpleName(), e);
                    }
                });

        log.info("SPI 扩展配置应用完成");
    }
}