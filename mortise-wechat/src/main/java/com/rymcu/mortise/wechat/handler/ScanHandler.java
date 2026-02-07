package com.rymcu.mortise.wechat.handler;

import com.rymcu.mortise.auth.event.QRCodeScanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 扫码事件处理器
 * <p>
 * 处理用户扫描二维码的事件，识别登录场景并发布事件
 *
 * @author Binary Wang
 * @author ronger
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScanHandler extends AbstractHandler {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 扫码登录场景值前缀
     */
    private static final String LOGIN_SCENE_PREFIX = "LOGIN_";

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        
        String event = wxMessage.getEvent();
        String eventKey = wxMessage.getEventKey();
        String openId = wxMessage.getFromUser();

        log.info("收到扫码事件 - event: {}, eventKey: {}, openId: {}", event, eventKey, openId);

        // 提取场景值
        String sceneStr = extractSceneStr(event, eventKey);
        if (sceneStr == null || sceneStr.isEmpty()) {
            log.warn("无效的场景值 - eventKey: {}", eventKey);
            return null;
        }

        // 判断是否为登录场景
        if (sceneStr.startsWith(LOGIN_SCENE_PREFIX)) {
            // 发布二维码扫描事件，由业务模块（api）处理
            log.info("检测到登录二维码扫描 - sceneStr: {}, openId: {}", sceneStr, openId);
            
            // 获取微信公众号 appId
            String appId = wxMpService.getWxMpConfigStorage().getAppId();
            
            // 发布扫码事件（包含 appId）
            QRCodeScanEvent scanEvent = new QRCodeScanEvent(
                this,
                sceneStr,
                openId,
                appId,
                event
            );
            
            eventPublisher.publishEvent(scanEvent);
            
            log.info("已发布二维码扫描事件: sceneStr={}, openId={}, appId={}, eventType={}",
                    sceneStr, openId, appId, event);
            
            // 返回提示消息
            return buildTextMessage(wxMessage, getResponseMessage(event));
        }

        // 其他扫码场景的处理...
        log.debug("非登录场景二维码 - sceneStr: {}", sceneStr);
        return null;
    }

    /**
     * 提取场景值
     */
    private String extractSceneStr(String event, String eventKey) {
        if (eventKey == null || eventKey.isEmpty()) {
            return null;
        }

        // 未关注用户扫码关注事件：eventKey = qrscene_场景值
        if (WxConsts.EventType.SUBSCRIBE.equals(event) && eventKey.startsWith("qrscene_")) {
            return eventKey.substring("qrscene_".length());
        }

        // 已关注用户扫码事件：eventKey = 场景值
        if (WxConsts.EventType.SCAN.equals(event)) {
            return eventKey;
        }

        return null;
    }

    /**
     * 获取响应消息
     */
    private String getResponseMessage(String event) {
        if (WxConsts.EventType.SUBSCRIBE.equals(event)) {
            return "感谢关注！登录成功，请返回电脑端查看。";
        } else {
            return "登录成功，请返回电脑端查看。";
        }
    }

    /**
     * 构建文本消息
     */
    private WxMpXmlOutMessage buildTextMessage(WxMpXmlMessage inMessage, String content) {
        return WxMpXmlOutMessage.TEXT()
                .content(content)
                .fromUser(inMessage.getToUser())
                .toUser(inMessage.getFromUser())
                .build();
    }
}
