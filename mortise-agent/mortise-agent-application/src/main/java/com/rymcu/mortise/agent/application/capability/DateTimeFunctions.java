package com.rymcu.mortise.agent.application.capability;

import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间相关工具
 */
@Configuration
public class DateTimeFunctions {
    
    /**
     * 获取当前时间
     */
    @Bean
    public FunctionCallback getCurrentTime() {
        return FunctionCallback.builder()
            .function("get_current_time", (String format) -> {
                if (format == null || format.isEmpty()) {
                    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                try {
                    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
                } catch (Exception e) {
                    return "Invalid format pattern: " + e.getMessage();
                }
            })
            .description("Get the current date and time. Optional parameter 'format' accepts patterns like 'yyyy-MM-dd HH:mm:ss'")
            .inputType(String.class)
            .build();
    }
    
    /**
     * 获取当前日期
     */
    @Bean
    public FunctionCallback getCurrentDate() {
        return FunctionCallback.builder()
            .function("get_current_date", (Void input) -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .description("Get the current date in yyyy-MM-dd format")
            .inputType(Void.class)
            .build();
    }
}
