package com.rymcu.mortise.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 二维码扫描事件
 * <p>
 * 当用户扫描二维码时发布此事件，由业务模块监听处理
 *
 * @author ronger
 * @since 1.0.0
 */
@Getter
public class QRCodeScanEvent extends ApplicationEvent {

    /**
     * 场景值（用于标识登录会话）
     */
    private final String sceneStr;

    /**
     * 扫码用户的唯一标识（如微信的 openId）
     */
    private final String userId;

    /**
     * 提供商（appId ）
     */
    private final String clientId;

    /**
     * 事件类型（subscribe: 扫码关注, scan: 已关注扫码）
     */
    private final String eventType;

    /**
     * 构造函数
     *
     * @param source    事件源
     * @param sceneStr  场景值
     * @param userId    用户标识（openId）
     * @param clientId  提供商（appId）
     * @param eventType 事件类型
     */
    public QRCodeScanEvent(Object source, String sceneStr, String userId, String clientId, String eventType) {
        super(source);
        this.sceneStr = sceneStr;
        this.userId = userId;
        this.clientId = clientId;
        this.eventType = eventType;
    }
}
