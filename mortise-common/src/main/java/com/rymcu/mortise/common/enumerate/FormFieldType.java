package com.rymcu.mortise.common.enumerate;

/**
 * 动态表单字段的 UI 渲染类型
 * <p>
 * 用于后端向前端描述配置字段应以何种控件渲染，实现"Schema 驱动"的动态表单。
 * 适用于任何需要动态渲染管理配置表单的场景（通知渠道、支付网关、文件存储等）。
 * <p>
 * 前端渲染规则：
 * <ul>
 *   <li>{@link #TEXT}     → {@code <UInput type="text" />}</li>
 *   <li>{@link #PASSWORD} → {@code <UInput type="password" />}，接口返回值脱敏为 "***"</li>
 *   <li>{@link #NUMBER}   → {@code <UInput type="number" />}</li>
 *   <li>{@link #BOOLEAN}  → {@code <UToggle />}</li>
 *   <li>{@link #EMAIL}    → {@code <UInput type="email" />}</li>
 *   <li>{@link #SELECT}   → {@code <USelect />}，字段定义中附带 options 列表</li>
 * </ul>
 *
 * @author ronger
 */
public enum FormFieldType {

    /** 普通单行文本输入框 */
    TEXT,

    /** 密码输入框，接口返回时已脱敏为 "***"，保存时若传 "***" 则保留原值 */
    PASSWORD,

    /** 数字输入框 */
    NUMBER,

    /** 布尔开关（Toggle） */
    BOOLEAN,

    /** 邮箱格式文本框，附带格式校验 */
    EMAIL,

    /** 下拉选择框，字段定义中需提供 options 列表 */
    SELECT
}
