package com.rymcu.mortise.auth.service;

import com.rymcu.mortise.auth.model.QRCodeResult;

/**
 * 二维码登录服务接口
 * <p>
 * 定义二维码登录的抽象接口，支持多种平台（微信、企业微信、钉钉等）的扫码登录实现。
 * <p>
 * 工作流程：
 * 1. 前端调用 createQRCode() 生成二维码
 * 2. 用户扫描二维码
 * 3. 平台推送扫码事件到服务器，触发 handleScanEvent()
 * 4. 前端轮询获取登录状态和 Token
 *
 * @author ronger
 * @since 1.0.0
 */
public interface QRCodeLoginService {

    /**
     * 生成扫码登录二维码
     *
     * @param appId 应用 ID
     * @param sceneStr      场景值（用于标识登录会话，建议使用 UUID）
     * @param expireSeconds 二维码有效期（秒），范围：60-2592000
     * @return 二维码结果，包含 ticket 和二维码 URL
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException         如果生成二维码失败
     */
    QRCodeResult createQRCode(String appId, String sceneStr, int expireSeconds);

    /**
     * 处理扫码事件
     * <p>
     * 当用户扫描二维码时，平台会推送事件到服务器，此方法负责：
     * 1. 更新二维码状态为"已扫描"
     * 2. 根据用户标识（如 openId）查找或创建用户
     * 3. 生成 JWT Token
     * 4. 存储 Token 到缓存供前端轮询获取
     *
     * @param sceneStr 场景值（与生成二维码时的 sceneStr 一致）
     * @param userId   扫码用户的唯一标识（如微信的 openId）
     * @throws IllegalArgumentException 如果场景值无效或已过期
     * @throws RuntimeException         如果处理失败
     */
    void handleScanEvent(String sceneStr, String userId);

    /**
     * 取消二维码登录
     * <p>
     * 清除二维码状态缓存，使二维码失效
     *
     * @param sceneStr 场景值
     */
    void cancelQRCode(String sceneStr);
}
