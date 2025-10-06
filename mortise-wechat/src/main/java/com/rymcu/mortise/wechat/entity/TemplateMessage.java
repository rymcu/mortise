package com.rymcu.mortise.wechat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信模板消息实体
 *
 * @author ronger
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMessage {

    /**
     * 接收者 OpenID
     */
    private String toUser;

    /**
     * 模板 ID
     */
    private String templateId;

    /**
     * 跳转链接（可选）
     */
    private String url;

    /**
     * 模板数据
     */
    @Builder.Default
    private List<TemplateData> data = new ArrayList<>();

    /**
     * 添加模板数据
     */
    public TemplateMessage addData(String name, String value) {
        return addData(name, value, null);
    }

    /**
     * 添加模板数据（带颜色）
     */
    public TemplateMessage addData(String name, String value, String color) {
        this.data.add(new TemplateData(name, value, color));
        return this;
    }

    /**
     * 模板数据项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateData {
        /**
         * 字段名称
         */
        private String name;

        /**
         * 字段值
         */
        private String value;

        /**
         * 字段颜色（可选）
         */
        private String color;
    }
}
