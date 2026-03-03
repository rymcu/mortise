package com.rymcu.mortise.notification.model;

import java.util.Map;

/**
 * 保存渠道配置请求体
 *
 * @param enabled 是否启用该渠道
 * @param values  各配置字段值（key 对应 {@link ChannelFieldDef#key()}）
 * @author ronger
 */
public record ChannelConfigSaveRequest(
        boolean enabled,
        Map<String, String> values
) {}
