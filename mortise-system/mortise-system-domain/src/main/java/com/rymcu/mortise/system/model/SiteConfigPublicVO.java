package com.rymcu.mortise.system.model;

import java.util.Map;

/**
 * 网站公开配置视图对象（无需鉴权，前端启动时加载）
 * <p>
 * 仅包含安全可公开的配置项（名称、Logo、Favicon、描述等），
 * 不包含任何敏感字段。
 *
 * @param values 所有公开配置项的 key → value 映射
 * @author ronger
 */
public record SiteConfigPublicVO(Map<String, String> values) {}
