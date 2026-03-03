package com.rymcu.mortise.notification.model;

import java.util.List;
import java.util.Map;

/**
 * 通知渠道配置视图对象（前端始终看到聚合后的整体）
 * <p>
 * {@code schema} 描述字段定义（代码驱动），
 * {@code values} 存放各字段的当前值（来自数据库，密码字段已脱敏为 "***"）。
 *
 * @param channel 渠道标识，对应 {@link com.rymcu.mortise.notification.enums.NotificationType#getCode()}
 * @param label   渠道中文名称
 * @param enabled 是否启用
 * @param schema  字段定义列表
 * @param values  字段当前值 key → value
 * @author ronger
 */
public record ChannelConfigVO(
        String channel,
        String label,
        boolean enabled,
        List<ChannelFieldDef> schema,
        Map<String, String> values
) {}
