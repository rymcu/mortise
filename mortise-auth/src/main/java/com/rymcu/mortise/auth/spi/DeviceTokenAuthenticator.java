package com.rymcu.mortise.auth.spi;

/**
 * 设备令牌认证 SPI。
 * <p>
 * 由设备管理模块（如 mortise-aiot）提供实现，
 * 消费方（如 mortise-ota-api）通过此接口校验设备 JWT 而无需直接依赖设备管理模块。
 *
 * @author ronger
 */
public interface DeviceTokenAuthenticator {

    /**
     * 校验设备令牌并返回已认证的设备信息。
     *
     * @param token 去除 Bearer 前缀后的 JWT 字符串
     * @return 认证通过的设备信息
     * @throws com.rymcu.mortise.common.exception.BusinessException 令牌无效、过期或设备状态异常时抛出
     */
    AuthenticatedDevice authenticate(String token);

    record AuthenticatedDevice(Long deviceId, String deviceCode) {
    }
}
