package com.rymcu.mortise.system.model;

import java.util.List;
import java.util.Map;

/**
 * 网站配置分组视图对象（前端始终看到聚合后的整体）
 * <p>
 * {@code schema} 描述字段定义（代码驱动），
 * {@code values} 存放各字段的当前值（来自数据库）。
 *
 * @param group  配置分组标识，如 site / seo
 * @param label  分组中文名称，如 基本信息 / SEO 设置
 * @param schema 字段定义列表
 * @param values 字段当前值（key → value）
 * @author ronger
 */
public record SiteConfigGroupVO(
        String group,
        String label,
        List<SiteConfigFieldDef> schema,
        Map<String, String> values
) {}
