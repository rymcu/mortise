package com.rymcu.mortise.wechat.config;

import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信开放平台基础配置
 * <p>只负责创建基础的、空的 WxOpenService Bean。该 Bean 总是存在于 Spring 上下文中。</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Configuration
public class WeChatOpenBaseConfiguration {

    @Bean
    public WxOpenService wxOpenService() {
        // 创建一个空的实例作为容器，它总是存在
        return new WxOpenServiceImpl();
    }
}
