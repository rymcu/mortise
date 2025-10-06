package com.rymcu.mortise.wechat.service;

import com.rymcu.mortise.wechat.config.WeChatOpenProperties;
import com.rymcu.mortise.wechat.exception.WeChatOpenNotConfiguredException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.api.impl.WxOpenInMemoryConfigStorage;
import org.springframework.stereotype.Service;

/**
 * 微信公众号动态管理器
 * <p>负责微信公众号配置的动态加载、更新、移除和查询</p>
 * <p>支持运行时热更新配置，无需重启应用</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicWeChatOpenServiceManager {

    private final WeChatConfigService configService;
    private final WxOpenService wxOpenService; // 注入空的 WxOpenService Bean

    // 使用 volatile 保证多线程下的可见性
    private volatile boolean isConfigured = false;

    /**
     * Bean 初始化后，从数据库加载初始配置
     */
    @PostConstruct
    public void init() {
        log.info("Performing initial load of WeChat Open Platform configuration...");
        this.reload();
    }

    /**
     * 从数据库重新加载配置。
     * <p>此方法可以被定时任务或管理后台接口调用，实现动态刷新。</p>
     * <p>使用 synchronized 保证线程安全，防止并发刷新导致状态不一致。</p>
     */
    public synchronized void reload() {
        try {
            WeChatOpenProperties properties = configService.loadDefaultOpenConfig();

            if (properties != null && properties.isEnabled()) {
                WxOpenInMemoryConfigStorage config = new WxOpenInMemoryConfigStorage();
                config.setComponentAppId(properties.getAppId());
                config.setComponentAppSecret(properties.getSecret());
                wxOpenService.setWxOpenConfigStorage(config);
                this.isConfigured = true;
                log.info("✓ WeChat Open Platform configuration loaded successfully. AppID: {}", properties.getAppId());
            } else {
                // 如果数据库中配置被禁用或删除，则清空内存中的配置
                this.isConfigured = false;
                log.info("WeChat Open Platform is not configured or disabled in the database.");
            }
        } catch (Exception e) {
            this.isConfigured = false;
            log.error("! Failed to load WeChat Open Platform configuration.", e);
        }
    }

    /**
     * 检查服务是否已配置并可用
     * @return true 如果可用
     */
    public boolean isAvailable() {
        return this.isConfigured;
    }

    /**
     * 获取 WxOpenService 实例
     * <p>这是业务代码应该调用的方法。</p>
     * @return 配置好的 WxOpenService 实例
     * @throws WeChatOpenNotConfiguredException 如果服务未配置或不可用
     */
    public WxOpenService getService() {
        if (!isConfigured) {
            throw new WeChatOpenNotConfiguredException("WeChat Open Platform service is not configured or disabled.");
        }
        return this.wxOpenService;
    }
}
