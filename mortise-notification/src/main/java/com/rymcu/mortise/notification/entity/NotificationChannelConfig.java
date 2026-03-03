package com.rymcu.mortise.notification.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知渠道配置持久化实体
 * <p>
 * 每行记录对应一个渠道（channel）下的一个配置项（config_key → config_value）。
 * 前端通过 API 获取的是各渠道所有配置项聚合后的整体 VO，而非此表的原始多行数据。
 *
 * @author ronger
 */
@Table(value = "mortise_notification_channel_config", schema = "mortise")
@Data
public class NotificationChannelConfig implements Serializable {

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 渠道标识，对应 {@link com.rymcu.mortise.notification.enums.NotificationType#getCode()}
     * 如：email、sms、wechat
     */
    private String channel;

    /**
     * 配置项 key，如 host、port、username、password
     */
    private String configKey;

    /**
     * 配置项值（密码类字段以明文存储，API 层负责脱敏输出）
     */
    private String configValue;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
