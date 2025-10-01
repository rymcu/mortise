package com.rymcu.mortise.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.system.annotation.DictAnnotationIntrospector;
import com.rymcu.mortise.system.service.DictService;
import com.rymcu.mortise.web.spi.JacksonConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * 字典翻译 Jackson 配置器
 * 
 * <p>实现 JacksonConfigurer SPI，为系统字典翻译功能提供序列化支持。</p>
 * 
 * <p><strong>功能特性：</strong></p>
 * <ul>
 *     <li>支持 @DictFormat 注解自动翻译字典值</li>
 *     <li>可配置翻译后的字段名后缀</li>
 *     <li>支持覆盖原始值或追加翻译字段</li>
 * </ul>
 * 
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * public class User {
 *     @DictFormat("user_status")
 *     private String status; // 原始值: "1"
 *     // 序列化后会添加: statusDict: "启用"
 * }
 * }</pre>
 * 
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass({DictService.class, ObjectMapper.class})
public class DictJacksonConfigurer implements JacksonConfigurer {

    private final DictService dictService;

    public DictJacksonConfigurer(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public void configureObjectMapper(ObjectMapper objectMapper) {
        // 设置字典注解内省器
        DictAnnotationIntrospector dictIntrospector = new DictAnnotationIntrospector(dictService);
        objectMapper.setAnnotationIntrospector(dictIntrospector);
        
        log.info("字典翻译序列化配置已应用");
    }

    @Override
    public int getOrder() {
        // 业务扩展配置，优先级中等
        return 200;
    }

    @Override
    public boolean isEnabled() {
        // 只有在 DictService 存在时才启用
        return dictService != null;
    }
}