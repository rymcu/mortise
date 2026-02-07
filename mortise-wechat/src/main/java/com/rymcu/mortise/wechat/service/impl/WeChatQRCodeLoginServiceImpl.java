package com.rymcu.mortise.wechat.service.impl;

import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.model.QRCodeResult;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.service.QRCodeLoginService;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 微信二维码登录服务实现
 * <p>
 * 基于微信公众号带参数二维码实现扫码登录功能
 * <p>
 * 职责：仅负责调用微信API创建二维码，不处理业务逻辑
 * 业务逻辑（如查找用户、生成Token）由 api 模块的事件监听器处理
 * <p>
 * API 文档: <a href="https://developers.weixin.qq.com/doc/service/api/qrcode/qrcodes/api_createqrcode.html">...</a>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service("weChatQRCodeLoginService")
@RequiredArgsConstructor
public class WeChatQRCodeLoginServiceImpl implements QRCodeLoginService {

    private final DynamicWeChatServiceManager weChatServiceManager;
    private final AuthCacheService authCacheService;

    @Override
    public QRCodeResult createQRCode(String appId, String sceneStr, int expireSeconds) {
        log.info("创建微信登录二维码 - appId： {}, sceneStr: {}, expireSeconds: {}", appId, sceneStr, expireSeconds);

        // 参数校验
        if (sceneStr == null || sceneStr.trim().isEmpty()) {
            throw new IllegalArgumentException("场景值不能为空");
        }
        if (expireSeconds < 60 || expireSeconds > 2592000) {
            throw new IllegalArgumentException("过期时间必须在 60-2592000 秒之间");
        }

        try {
            // 获取默认微信公众号服务
            WxMpService wxMpService;
            if (StringUtils.isNotBlank(appId)) {
                wxMpService = weChatServiceManager.getServiceByAppId(appId);
            } else {
                wxMpService = weChatServiceManager.getDefaultService();
            }
            if (wxMpService == null) {
                throw new RuntimeException("未找到可用的微信公众号服务，请检查配置");
            }

            // 调用微信 API 创建临时二维码（字符串类型场景值）
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService()
                    .qrCodeCreateTmpTicket(sceneStr, expireSeconds);

            // 缓存二维码状态为"待扫描"
            authCacheService.storeOAuth2QrcodeState(sceneStr, QrcodeState.WAITED.getValue());

            log.info("微信登录二维码创建成功 - sceneStr: {}, ticket: {}, url: {}",
                    sceneStr, ticket.getTicket(), ticket.getUrl());

            return QRCodeResult.builder()
                    .ticket(ticket.getTicket())
                    .url(ticket.getUrl())
                    .expireSeconds(ticket.getExpireSeconds())
                    .sceneStr(sceneStr)
                    .build();

        } catch (WxErrorException e) {
            log.error("创建微信登录二维码失败 - sceneStr: {}, errorCode: {}, errorMsg: {}",
                    sceneStr, e.getError().getErrorCode(), e.getError().getErrorMsg(), e);
            throw new RuntimeException("创建微信登录二维码失败: " + e.getError().getErrorMsg(), e);
        } catch (Exception e) {
            log.error("创建微信登录二维码失败 - sceneStr: {}", sceneStr, e);
            throw new RuntimeException("创建微信登录二维码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void handleScanEvent(String sceneStr, String userId) {
        // 这个方法不再需要，由 api 模块的事件监听器处理
        // 保留空实现以符合接口要求
        log.debug("handleScanEvent 已废弃，由事件监听器处理 - sceneStr: {}, userId: {}", sceneStr, userId);
    }

    @Override
    public void cancelQRCode(String sceneStr) {
        log.info("取消二维码登录 - sceneStr: {}", sceneStr);

        if (sceneStr == null || sceneStr.trim().isEmpty()) {
            log.warn("场景值为空，无法取消");
            return;
        }

        // 清除二维码缓存
        authCacheService.removeOAuth2QrcodeState(sceneStr);

        log.info("二维码登录已取消 - sceneStr: {}", sceneStr);
    }
}
