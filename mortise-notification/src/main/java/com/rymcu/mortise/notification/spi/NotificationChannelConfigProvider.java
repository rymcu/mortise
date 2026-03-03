package com.rymcu.mortise.notification.spi;

import com.rymcu.mortise.notification.enums.NotificationType;

import java.util.Map;
import java.util.Optional;

/**
 * 通知渠道配置提供者 SPI
 * <p>
 * 各 {@link NotificationSender} 实现通过此接口获取运行时配置，
 * 从而与具体的配置来源（数据库、yml 文件等）解耦。
 * <p>
 * 默认实现由 {@code NotificationChannelConfigServiceImpl} 提供（数据库读取 + 缓存）。
 *
 * @author ronger
 */
public interface NotificationChannelConfigProvider {

    /**
     * 获取指定渠道的全量配置（key → value）
     *
     * @param type 通知类型
     * @return 配置 Map；若数据库中无任何配置则返回空 Map
     */
    Map<String, String> getConfig(NotificationType type);

    /**
     * 获取指定渠道的单个配置项
     *
     * @param type 通知类型
     * @param key  配置项 key
     * @return 配置值（Optional 空值表示未配置）
     */
    Optional<String> getValue(NotificationType type, String key);

    /**
     * 判断该渠道是否已启用
     *
     * @param type 通知类型
     * @return true 表示已启用
     */
    boolean isEnabled(NotificationType type);

    /**
     * 主动刷新指定渠道的配置缓存
     * <p>
     * 通常在管理端修改完配置后调用，使新配置立即生效。
     *
     * @param type 通知类型
     */
    void refreshCache(NotificationType type);
}
