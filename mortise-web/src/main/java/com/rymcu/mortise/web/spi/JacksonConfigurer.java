package com.rymcu.mortise.web.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;

/**
 * Jackson 配置器 SPI 接口
 * 
 * <p>允许各业务模块扩展 Jackson 序列化配置，实现模块化的序列化逻辑。</p>
 * 
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *     <li>业务字典翻译序列化（system 模块）</li>
 *     <li>敏感数据脱敏序列化（auth 模块）</li>
 *     <li>日期时间格式化（各业务模块）</li>
 *     <li>自定义注解处理器</li>
 * </ul>
 * 
 * <p><strong>实现步骤：</strong></p>
 * <ol>
 *     <li>实现此接口并标注 @Component</li>
 *     <li>重写 configureObjectMapper 方法</li>
 *     <li>设置合适的执行顺序（getOrder）</li>
 *     <li>Spring Boot 会自动发现并应用配置</li>
 * </ol>
 * 
 * @author ronger
 * @since 1.0.0
 */
public interface JacksonConfigurer extends Ordered {

    /**
     * 配置 ObjectMapper
     * 
     * @param objectMapper Spring 管理的 ObjectMapper 实例
     */
    void configureObjectMapper(ObjectMapper objectMapper);

    /**
     * 获取配置器的执行顺序
     * 
     * <p>数值越小，优先级越高。建议的顺序规范：</p>
     * <ul>
     *     <li>基础配置：100-199（如日期格式、基本序列化规则）</li>
     *     <li>业务扩展：200-299（如字典翻译、数据转换）</li>
     *     <li>安全相关：300-399（如敏感数据处理）</li>
     *     <li>其他扩展：400+</li>
     * </ul>
     * 
     * @return 执行顺序，数值越小优先级越高
     */
    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * 检查配置器是否启用
     * 
     * @return true: 启用, false: 禁用
     */
    default boolean isEnabled() {
        return true;
    }
}