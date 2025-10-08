package com.rymcu.mortise.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 2025/10/8 21:18.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.config
 */
@Configuration
public class WeChatOauth2Config {
    /**
     * 创建一个专门用于与微信 API 通信的 RestTemplate Bean。
     * <p>
     * 这个 RestTemplate 经过特殊配置，可以处理微信返回的 text/plain 类型的 JSON 响应。
     * 使用 @Qualifier("weChatRestTemplate") 来确保在需要时注入正确的实例。
     *
     * @return 配置好的 RestTemplate 实例
     */
    @Bean
    @Qualifier("weChatRestTemplate")
    public RestTemplate weChatRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 配置 JSON 消息转换器以支持 text/plain
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN, // 这是关键，处理微信返回的 Content-Type
                new MediaType("application", "*+json")
        ));

        // 设置消息转换器列表
        restTemplate.setMessageConverters(Arrays.asList(
                new FormHttpMessageConverter(), // 用于发送表单数据
                jsonConverter                  // 用于处理 JSON 响应
        ));

        // 配置 OAuth2 错误处理器
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        return restTemplate;
    }
}
