package com.rymcu.mortise;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Servlet 初始化器
 * 用于 WAR 包部署到外部容器
 *
 * @author ronger
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MortiseApplication.class);
    }
}
