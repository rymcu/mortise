package com.rymcu.mortise.system.model;

import java.util.Map;

/**
 * 保存网站配置请求体
 *
 * @param values 各配置字段值（key 对应 {@link SiteConfigFieldDef#key()}）
 * @author ronger
 */
public record SiteConfigSaveRequest(
        Map<String, String> values
) {}
