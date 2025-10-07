package com.rymcu.mortise.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created on 2025/10/7 12:54.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web.config
 */
@Configuration
public class WebConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
