package com.rymcu.mortise.notification.entity;

import com.rymcu.mortise.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 通知消息实体
 *
 * @author ronger
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 接收者（邮箱、手机号、用户ID等）
     */
    private String receiver;

    /**
     * 标题/主题
     */
    private String subject;

    /**
     * 内容
     */
    private String content;

    /**
     * 模板名称（用于模板引擎渲染）
     */
    private String template;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 附加数据
     */
    private Map<String, Object> metadata;

    /**
     * 是否异步发送
     */
    @Builder.Default
    private Boolean async = true;
}
