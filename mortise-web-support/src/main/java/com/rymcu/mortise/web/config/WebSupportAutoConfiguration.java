package com.rymcu.mortise.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for web support components.
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.rymcu.mortise.web")
public class WebSupportAutoConfiguration {
}
