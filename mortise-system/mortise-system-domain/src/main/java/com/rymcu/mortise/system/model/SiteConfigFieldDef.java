package com.rymcu.mortise.system.model;

import com.rymcu.mortise.common.enumerate.FormFieldType;

import java.util.List;

/**
 * 网站配置单个字段的 Schema 定义
 * <p>
 * Schema 存在代码中（不入库），描述"有哪些字段以及如何渲染"；
 * 字段的实际值（values）单独存储于数据库。
 *
 * @param key          字段唯一标识，对应 mortise_system_config 表的 config_key
 * @param label        前端展示名称
 * @param type         UI 渲染类型（来自通用的 {@link FormFieldType}）
 * @param required     是否必填
 * @param placeholder  输入框占位提示
 * @param defaultValue 默认值（可为 null）
 * @param options      SELECT 类型的可选项列表（其他类型传 null）
 * @author ronger
 */
public record SiteConfigFieldDef(
        String key,
        String label,
        FormFieldType type,
        boolean required,
        String placeholder,
        String defaultValue,
        List<SelectOption> options
) {

    /**
     * SELECT 选项
     *
     * @param label 展示名称
     * @param value 选项值
     */
    public record SelectOption(String label, String value) {}
}
