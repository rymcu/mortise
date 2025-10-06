package com.rymcu.mortise.wechat.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.wechat.config.WeChatMpProperties;
import com.rymcu.mortise.wechat.config.WeChatOpenProperties;
import com.rymcu.mortise.wechat.entity.WeChatConfig;

/**
 * 微信配置服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface WeChatConfigService extends IService<WeChatConfig> {

    WeChatMpProperties loadDefaultMpConfig();

    WeChatMpProperties loadMpConfigByAccountId(Long accountId);

    WeChatMpProperties loadMpConfigByAppId(String appId);

    WeChatOpenProperties loadDefaultOpenConfig();

    WeChatOpenProperties loadOpenConfigByAccountId(Long accountId);

    void refreshCache();
}
