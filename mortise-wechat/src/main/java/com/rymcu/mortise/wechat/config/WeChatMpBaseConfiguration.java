package com.rymcu.mortise.wechat.config;

import com.rymcu.mortise.wechat.handler.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static me.chanjar.weixin.common.api.WxConsts.EventType.*;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.LOCATION;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.CustomerService.*;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.POI_CHECK_NOTIFY;

/**
 * 微信公众号基础配置
 * <p>负责创建基础的、空的、可变的 Bean 实例。所有动态加载逻辑移至 DynamicWxMpManager。</p>
 * <p>这个配置类保持简洁，只负责 Bean 的声明，不处理复杂的业务逻辑。</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeChatMpBaseConfiguration {

    // 注入所有的 Handler，因为它们是无状态的，可以作为单例 Bean
    private final LogHandler logHandler;
    private final NullHandler nullHandler;
    private final KfSessionHandler kfSessionHandler;
    private final StoreCheckNotifyHandler storeCheckNotifyHandler;
    private final LocationHandler locationHandler;
    private final MenuHandler menuHandler;
    private final MsgHandler msgHandler;
    private final UnsubscribeHandler unsubscribeHandler;
    private final SubscribeHandler subscribeHandler;
    private final ScanHandler scanHandler;

    /**
     * 创建一个空的、支持多账号的 WxMpService 单例。
     * <p>这个 Bean 总是存在，解决了 Optional 注入问题。</p>
     * <p>配置的加载和管理由 DynamicWxMpManager 负责。</p>
     */
    @Bean
    public WxMpService wxMpService() {
        log.info("Creating empty WxMpService bean for dynamic configuration");
        return new WxMpServiceImpl();
    }

    /**
     * 创建消息路由器。
     * <p>由于 wxMpService Bean 总是存在，这里可以安全地直接注入。</p>
     * <p>路由规则在应用启动时就确定，不需要动态变更。</p>
     */
    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

        // 记录所有事件的日志 （异步执行）
        newRouter.rule().handler(this.logHandler).next();

        // 客服会话管理事件
        newRouter.rule().async(false).msgType(EVENT).event(KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_CLOSE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_SWITCH_SESSION).handler(this.kfSessionHandler).end();

        // 门店审核事件
        newRouter.rule().async(false).msgType(EVENT).event(POI_CHECK_NOTIFY).handler(this.storeCheckNotifyHandler).end();
        
        // 自定义菜单事件
        newRouter.rule().async(false).msgType(EVENT).event(CLICK).handler(this.menuHandler).end();
        
        // 点击菜单连接事件
        newRouter.rule().async(false).msgType(EVENT).event(VIEW).handler(this.nullHandler).end();
        
        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();
        
        // 取消关注事件
        newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(this.unsubscribeHandler).end();
        
        // 上报地理位置事件
        newRouter.rule().async(false).msgType(EVENT).event(LOCATION).handler(this.locationHandler).end();
        
        // 接收地理位置消息
        newRouter.rule().async(false).msgType(LOCATION).handler(this.locationHandler).end();
        
        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(SCAN).handler(this.scanHandler).end();
        
        // 默认处理器
        newRouter.rule().async(false).handler(this.msgHandler).end();

        log.info("✓ 微信公众号消息路由器初始化成功");
        return newRouter;
    }
}